package gg.sep.securityrobot.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.exceptions.DuplicateCommandException;
import gg.sep.securityrobot.exceptions.SecurityRobotFatal;
import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;

/**
 * Command runner and dispatcher for an instance of the bot.
 * This class need only be instantiated once, with the bot class passed in.
 */
@Log4j2
public class CommandRunner {

    private Collection<CommandHandler> commandHandlers = new ArrayList<>();

    @Getter
    private Map<String, Pair<Method, CommandLevel>> commandList = new HashMap<>();

    /**
     * Construct an instance for the specified Bot class.
     * IMPORTANT: All command commandHandlers should be sub-packages of the bot's package,
     *            since this package is what's used for finding the command handling classes.
     * @param bot Instance of the bot to use for the command runner.
     * @throws SecurityRobotFatal Exception thrown if duplicate commands are found while initializing.
     */
    public CommandRunner(final SecurityRobot bot) throws SecurityRobotFatal {
        final Reflections reflections = new Reflections(bot.getClass().getPackageName());

        final Set<Class<? extends CommandHandler>> subTypes = reflections.getSubTypesOf(CommandHandler.class);

        for (final Class<? extends CommandHandler> clazz : subTypes) {
            final Method[] methods = clazz.getDeclaredMethods();
            for (final Method method : methods) {
                final Optional<ChatCommand> annotation = checkForDuplicateCommands(bot, method);
                if (annotation.isEmpty()) {
                    continue;
                }
                commandList.put(annotation.get().value(), Pair.of(method, annotation.get().level()));
            }
        }
        addHandlers(subTypes);
    }

    /**
     * Parses a Twitch chat message starting with the command prefix and runs the command if it is valid.
     *
     * Messages passed to this method should have first been validated for containing
     * the prefix.
     * @param message Twitch chat message which triggered the command.
     */
    public void parseCommand(final TwitchChannelMessage message) {
        final Optional<String> command = extractCommand(message);
        command.ifPresent(s -> dispatchCommand(new CommandEvent(s, message)));
    }

    private void addHandlers(final Collection<Class<? extends CommandHandler>> handlers) {
        for (final Class<? extends CommandHandler> handlerClass : handlers) {
            if (!handlerClass.isInterface()) {
                try {
                    this.commandHandlers.add(handlerClass.getConstructor().newInstance());
                } catch (final Exception e) {
                    log.error(e);
                }
            }
        }
    }

    private static Optional<String> extractCommand(final TwitchChannelMessage message) {
        if (!message.getMessage().startsWith(SecurityRobot.COMMAND_PREFIX)) {
            return Optional.empty();
        }

        final String[] splitMsg = message.getMessage().split(" ");
        if (splitMsg.length > 0) {
            final String commandStr = splitMsg[0]
                .replaceFirst("\\" + SecurityRobot.COMMAND_PREFIX, "")
                .trim();
            return Optional.of(commandStr);
        }
        return Optional.empty();
    }


    private void dispatchCommand(final CommandEvent event) {
        for (final CommandHandler handler : commandHandlers) {
            dispatchEventTo(event, handler);
        }
    }

    private void dispatchEventTo(final CommandEvent event, final CommandHandler handler) {
        final Method[] methods = handler.getClass().getDeclaredMethods();

        for (final Method method : methods) {
            if (canHandleEvent(method, event)) {
                try {
                    method.setAccessible(true);
                    method.invoke(handler, event);
                    log.info("Command invoked: [{}] | Method: [{}#{}]", event.getName(),
                        method.getDeclaringClass().getSimpleName(), method.getName());
                } catch (final Exception e) {
                    log.error(e);
                }
            }
        }
    }

    private boolean canHandleEvent(final Method method, final CommandEvent event) {
        final ChatCommand annotation = method.getAnnotation(ChatCommand.class);
        if (annotation != null) {

            // check if the event name matches first to prevent unnecessary API calls
            return (annotation.value().equalsIgnoreCase(event.getName()) &&
                event.getChannelMessage().getAuthor().canRunCommandLevel(annotation.level()));
        }
        return false;
    }

    private Optional<ChatCommand> checkForDuplicateCommands(final SecurityRobot bot,
                                                            final Method method) throws DuplicateCommandException {
        final ChatCommand annotation = method.getAnnotation(ChatCommand.class);
        if (annotation == null) {
            return Optional.empty();
        }
        final String commandName = annotation.value().toLowerCase();

        if (commandList.containsKey(commandName)) {
            final Pair<Method, CommandLevel> command = commandList.get(commandName);
            final String reason = String.format(
                "Command with name '%s' assigned to more than one method. Already assigned to: %s:%s(%s)",
                commandName, command.getLeft().getDeclaringClass().getSimpleName(), command.getLeft().getName(),
                getMethodSignature(command.getLeft())
            );
            bot.shutdown(reason);
            throw new DuplicateCommandException(reason);
        }
        return Optional.of(annotation);
    }

    private static String getMethodSignature(final Method method) {
        final List<Class> parameters = Arrays.asList(method.getParameterTypes());
        return parameters.stream()
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", "));
    }
}
