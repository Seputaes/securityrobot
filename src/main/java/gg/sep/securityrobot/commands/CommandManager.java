package gg.sep.securityrobot.commands;

import static gg.sep.securityrobot.SecurityRobot.REDIS_PREFIX;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.commands.handlers.custom.CustomCommandMetadata;
import gg.sep.securityrobot.exceptions.DuplicateCommandException;
import gg.sep.securityrobot.exceptions.SecurityRobotFatal;
import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;
import gg.sep.securityrobot.models.twitch.tmi.TwitchMessageAuthor;

/**
 * Command runner and dispatcher for an instance of the bot.
 * This class need only be instantiated once, with the bot class passed in.
 */
@Log4j2
public class CommandManager {

    private static final String G_CUSTOM_COM_KEY = REDIS_PREFIX + "gcustomcom";
    private static final String G_CUSTOM_META_KEY_F = G_CUSTOM_COM_KEY + ":%s:metadata";

    private final SecurityRobot securityRobot;
    private final JedisPool jedisPool;
    @Getter private Set<Command> allCommands = new HashSet<>();
    @Getter private Map<String, Command> commandTriggers = new HashMap<>();

    /**
     * Construct an instance for the specified Bot class.
     * IMPORTANT: All command commandHandlers should be sub-packages of the bot's package,
     *            since this package is what's used for finding the command handling classes.
     * @param securityRobot Instance of the bot to use for the command manager.
     * @throws SecurityRobotFatal Exception thrown if duplicate commands are found while initializing.
     */
    public CommandManager(final SecurityRobot securityRobot) throws SecurityRobotFatal {
        this.securityRobot = securityRobot;
        this.jedisPool = securityRobot.getJedisPool();

        // get built in bot commands and map each of the triggers to the command
        final Set<Command> builtInCommands = getBuiltInCommands();
        builtInCommands.forEach(c -> c.getTriggerStrings().forEach(t -> commandTriggers.put(t, c)));

        // get all custom commands and check for collisions with the built in
        // built in commands will always win
        final Set<Command> customCommands = removeDuplicates(builtInCommands, getCustomCommands());
        customCommands.forEach(c -> c.getTriggerStrings().forEach(t -> commandTriggers.put(t, c)));

        allCommands.addAll(builtInCommands);
        allCommands.addAll(customCommands);
    }

    /**
     * Adds a new global custom command to the database and registers it into the bot.
     *
     * This does not do any checks to see if the command is a duplicate. It will overwrite any existing commands
     * which match the command name.
     * @param commandName Name of the command (its primary trigger).
     * @param response Text to respond to the user when it is triggered.
     * @param level Default level needed to invoke the command.
     */
    public void addCustomCommand(final String commandName, final String response, final double level) {
        final String cleanName = commandName.trim().toLowerCase();
        final String metadataKey = String.format(G_CUSTOM_META_KEY_F, cleanName);
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("level", String.valueOf(level));
        metadata.put("response", response);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(metadataKey, metadata);
            jedis.sadd(G_CUSTOM_COM_KEY, cleanName);
        }
        // add it to the command set and triggers
        final CustomCommandMetadata commandMetadata = parseMetadata(cleanName, metadata);
        final Command command = Command.fromCommandMetadata(commandMetadata);
        allCommands.add(command);
        command.getTriggerStrings().forEach(t -> commandTriggers.put(t, command));
    }

    /**
     * Deletes a global custom command by its name or one of its aliases.
     * @param commandName Name or alias of the command to delete.
     */
    public void delCustomCommand(final String commandName) {
        final String cleanName = commandName.trim().toLowerCase();
        final String metadataKey = String.format(G_CUSTOM_META_KEY_F, cleanName);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(metadataKey);
            jedis.srem(G_CUSTOM_COM_KEY, cleanName);
        }
        // remove it from the command set
        final Optional<Command> curCommand = allCommands.stream()
            .filter(c -> c.getTriggerStrings().contains(cleanName))
            .findFirst();
        // remove all the aliases and the command
        if (curCommand.isPresent()) {
            curCommand.get().getTriggerStrings().forEach(t -> commandTriggers.remove(t));
            allCommands.remove(curCommand.get());
        }
    }

    private Set<Command> getBuiltInCommands() throws SecurityRobotFatal {
        final Set<Command> builtInCommands = new HashSet<>();
        final Reflections reflect = new Reflections(securityRobot.getClass().getPackageName(),
            new MethodAnnotationsScanner());
        final Set<Method> commandMethods = reflect.getMethodsAnnotatedWith(ChatCommand.class);

        for (final Method method : commandMethods) {
            final Optional<ChatCommand> annotation = checkValidCommand(securityRobot, method);
            if (annotation.isPresent()) {
                final Command command = Command.fromAnnotation(annotation.get(), method);
                builtInCommands.add(command);
            }
        }
        return builtInCommands;
    }

    private Set<Command> getCustomCommands() {
        final Set<Command> customCommands = new HashSet<>();
        try (Jedis jedis = jedisPool.getResource()) {
            final Set<String> commandNames = jedis.smembers(G_CUSTOM_COM_KEY);
            if (!commandNames.isEmpty()) {
                for (final String commandName : commandNames) {
                    final Optional<CustomCommandMetadata> metadata = getMetadata(commandName);
                    metadata.ifPresent(m -> customCommands.add(Command.fromCommandMetadata(m)));
                }
            }
        }
        return customCommands;
    }

    private boolean customCommandIsDuplicate(final String commandName) {
        final String cleanName = commandName.trim().toLowerCase();
        return commandTriggers.containsKey(cleanName);
    }

    private Set<Command> removeDuplicates(final Set<Command> builtIn, final Set<Command> custom) {
        final Set<Command> duplicatesRemoved = new HashSet<>();

        for (final Command customCom : custom) {
            for (final Command builtInCommand : builtIn) {
                if (customCom.isDuplicate(builtInCommand)) {
                    log.error("Custom command conflicts with internal command. It will not be loaded. " +
                        "Command: {}", customCom.getName());
                    continue; // skip adding it
                }
                duplicatesRemoved.add(customCom);
            }
        }
        return duplicatesRemoved;
    }

    private Optional<CustomCommandMetadata> getMetadata(final String commandName) {
        final String cleanName = commandName.trim().toLowerCase();
        try (Jedis jedis = jedisPool.getResource()) {
            if (!customCommandExists(jedis, cleanName)) {
                return Optional.empty();
            }
            final String metadataKey = String.format(G_CUSTOM_META_KEY_F, cleanName);
            final Map<String, String> metadata = jedis.hgetAll(metadataKey);
            return Optional.of(parseMetadata(cleanName, metadata));
        }
    }

    private boolean customCommandExists(final Jedis jedis, final String commandName) {
        return jedis.sismember(G_CUSTOM_COM_KEY, commandName.toLowerCase());
    }

    private CustomCommandMetadata parseMetadata(final String commandName, final Map<String, String> metadata) {
        final Gson gson = new Gson();
        final JsonElement jsonElement = gson.toJsonTree(metadata);
        final CustomCommandMetadata commandMetadata = gson.fromJson(jsonElement, CustomCommandMetadata.class);
        commandMetadata.setCommandName(commandName);
        return commandMetadata;
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
        command.ifPresent(com -> invokeCommand(new CommandEvent(this.securityRobot, com, this, message)));
    }

    private Optional<Command> extractCommand(final TwitchChannelMessage message) {
        if (!message.getMessage().startsWith(SecurityRobot.COMMAND_PREFIX)) {
            return Optional.empty();
        }

        final String[] splitMsg = message.getMessage().split(" ");
        if (splitMsg.length > 0) {
            final String commandStr = splitMsg[0]
                .replaceFirst("\\" + SecurityRobot.COMMAND_PREFIX, "")
                .trim().toLowerCase();
            return Optional.ofNullable(commandTriggers.get(commandStr));
        }
        return Optional.empty();
    }

    private void invokeCommand(final CommandEvent event) {
        if (commandCanRun(event)) {
            try {
                // if it's a custom command, return the response
                if (event.getCommand().isCustom()) {
                    final String response = event.getCommand().getCustomMetadata().getResponse();
                    event.reply(response);
                    return;
                }
                // otherwise, invoke the internal command method
                event.getCommand().getMethod().invoke(null, event);
                event.getCommand().getLastExecuted().reset().start();
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

        for (final Command existingCommand : allCommands) {
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

    private boolean commandCanRun(final CommandEvent event) {
        final TwitchMessageAuthor author = event.getChannelMessage().getAuthor();
        final Command command = event.getCommand();

        return author.canRunCommandLevel(event.getCommand().getLevel()) && command.cooldownElapsed();
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
