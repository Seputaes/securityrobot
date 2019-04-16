package gg.sep.securityrobot.commands.handlers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.commands.ChatCommand;
import gg.sep.securityrobot.commands.Command;
import gg.sep.securityrobot.commands.CommandEvent;
import gg.sep.securityrobot.commands.CommandLevel;
import gg.sep.securityrobot.commands.CommandManager;
import gg.sep.securityrobot.models.twitch.tmi.TwitchMessageAuthor;

/**
 * Collection of General commands for the bot.
 * TODO: These are work in progress and generally designed for testing while in development.
 */
@Log4j2
@UtilityClass
public class GeneralCommands {

    /**
     * All users should be able to execute this command.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "all", description = "all desc", level = CommandLevel.ALL, cooldown = 10)
    public static void all(final CommandEvent event) {
        event.reply("All command");
    }

    /**
     * Replies "Pong!" if the command author is the channel's broadcaster.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "ping", aliases = {"foo", "bar"}, description = "ping desc", level = CommandLevel.BROADCASTER)
    public static void ping(final CommandEvent event) {
        event.mention("Pong!");
    }

    /**
     * Should not reply at all since the command is disabled.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "disabled", level = CommandLevel.DISABLED)
    public static void disabled(final CommandEvent event) {
        log.error("Error with DISABLED Command Level. This command should never be triggered");
        event.reply("ERROR: This command should have never been triggered.");
    }

    /**
     * Prints out a list of all commands that the user can run.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "commands", description = "Lists the commands that you have access to run",
        level = CommandLevel.ALL, showInCommandList = false)
    public static void commands(final CommandEvent event) {
        final CommandManager commandManager = event.getChannelMessage().getSecurityRobot().getCommandManager();
        final TwitchMessageAuthor author = event.getChannelMessage().getAuthor();

        final Set<Command> commandSet = commandManager.getAllCommands().stream()
            .filter(Command::isShownInCommandList)
            .collect(Collectors.toSet());

        final String commandString = commandSet.stream()
            .map(c -> SecurityRobot.COMMAND_PREFIX + c.getName())
            .sorted()
            .collect(Collectors.joining(", "));
        event.mention(commandString);
    }

    /**
     * Replies with the help message for the given command.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "help", description = "help command desc", level = CommandLevel.ALL)
    public static void help(final CommandEvent event) {
        final Optional<String> commandName = event.getCommandText();
        final TwitchMessageAuthor author = event.getChannelMessage().getAuthor();
        if (commandName.isEmpty()) {
            event.mention("You need to specify a command!");
            return;
        }

        final Command command = event.getCommandManager().getCommandTriggers().get(commandName.get());
        if (command != null) {
            if (author.canRunCommandLevel(command.getLevel())) {
                event.mention(command.getHelp());
            }
        } else {
            event.mention("That command was not found.");
        }
    }
}
