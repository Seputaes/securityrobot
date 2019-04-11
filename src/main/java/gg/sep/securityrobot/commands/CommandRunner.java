package gg.sep.securityrobot.commands;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.exceptions.DuplicateCommandException;
import gg.sep.securityrobot.exceptions.SecurityRobotFatal;
import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;
import gg.sep.securityrobot.models.twitch.tmi.TwitchMessageAuthor;

/**
 * Command runner and dispatcher for an instance of the bot.
 * This class need only be instantiated once, with the bot class passed in.
 */
@Log4j2
public class CommandRunner {

    @Getter
    private Set<Command> commandList = new HashSet<>();

    /**
     * Construct an instance for the specified Bot class.
     * IMPORTANT: All command commandHandlers should be sub-packages of the bot's package,
     *            since this package is what's used for finding the command handling classes.
     * @param bot Instance of the bot to use for the command runner.
     * @throws SecurityRobotFatal Exception thrown if duplicate commands are found while initializing.
     */
    public CommandRunner(final SecurityRobot bot) throws SecurityRobotFatal {
        final Reflections reflections = new Reflections(bot.getClass().getPackageName(),
            new MethodAnnotationsScanner());

        final Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(ChatCommand.class);

        for (final Method method : annotatedMethods) {
            final Optional<ChatCommand> annotation = checkValidCommand(bot, method);
            annotation.ifPresent(a -> commandList.add(Command.fromAnnotation(a, method)));
        }
    }

    /**
     * Parses a Twitch chat message starting with the command prefix and runs the command if it is valid.
     *
     * Messages passed to this method should have first been validated for containing
     * the prefix.
     * @param message Twitch chat message which triggered the command.
     */
    public void parseCommand(final TwitchChannelMessage message) {
        final Optional<Command> command = extractCommand(message);
        command.ifPresent(com -> dispatchCommand(new CommandEvent(com, message)));
    }

    private Optional<Command> extractCommand(final TwitchChannelMessage message) {
        if (!message.getMessage().startsWith(SecurityRobot.COMMAND_PREFIX)) {
            return Optional.empty();
        }

        final String[] splitMsg = message.getMessage().split(" ");
        if (splitMsg.length > 0) {
            final String commandStr = splitMsg[0]
                .replaceFirst("\\" + SecurityRobot.COMMAND_PREFIX, "")
                .trim();

            for (final Command botCommand : commandList) {
                if (botCommand.handlesCommand(commandStr)) {
                    return Optional.of(botCommand);
                }
            }
        }
        return Optional.empty();
    }


    private void dispatchCommand(final CommandEvent event) {

        final TwitchMessageAuthor author = event.getChannelMessage().getAuthor();

        if (author.canRunCommandLevel(event.getCommand().getLevel())) {
            try {
                event.getCommand().getMethod().invoke(null, event);
            } catch (final ReflectiveOperationException e) {
                log.error(e);
            }
        }
    }

    private Optional<ChatCommand> checkValidCommand(final SecurityRobot bot,
                                                    final Method method) throws DuplicateCommandException {
        final ChatCommand annotation = method.getAnnotation(ChatCommand.class);
        if (annotation == null) {
            return Optional.empty();
        }
        final Command newCommand = Command.fromAnnotation(annotation, method);

        if (!Modifier.isStatic(method.getModifiers())) {
            final String reason = String.format(
                "Command [%s:%s(%s)] is not declared as static. Commands methods must be static.",
                method.getDeclaringClass().getSimpleName(), method.getName(), getMethodSignature(method));
            shutdownAndThrow(bot, reason);
        }

        for (final Command existingCommand : commandList) {
            if (existingCommand.isDuplicate(newCommand)) {
                final String reason = String.format(
                    "Command with name '%s' assigned to more than one method. Already assigned to: %s:%s(%s)",
                    newCommand.getName(), existingCommand.getMethod().getDeclaringClass().getSimpleName(),
                    existingCommand.getMethod().getName(),
                    getMethodSignature(existingCommand.getMethod())
                );
                shutdownAndThrow(bot, reason);
            }
        }
        return Optional.of(annotation);
    }

    private static String getMethodSignature(final Method method) {
        final List<Class> parameters = Arrays.asList(method.getParameterTypes());
        return parameters.stream()
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", "));
    }

    private void shutdownAndThrow(final SecurityRobot bot, final String reason) throws DuplicateCommandException {
        bot.shutdown(reason);
        throw new DuplicateCommandException(reason);
    }
}
