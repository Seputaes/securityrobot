package gg.sep.securityrobot.commands.handlers;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.commands.ChatCommand;
import gg.sep.securityrobot.commands.Command;
import gg.sep.securityrobot.commands.CommandEvent;
import gg.sep.securityrobot.commands.CommandLevel;
import gg.sep.securityrobot.commands.CommandRunner;
import gg.sep.securityrobot.models.twitch.tmi.TwitchMessageAuthor;

/**
 * Collection of General commands for the bot.
 * TODO: These are work in progress and generally designed for testing while in development.
 */
@Log4j2
public final class GeneralCommands {

    private GeneralCommands() { }

    /**
     * All users should be able to execute this command.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "all", level = CommandLevel.ALL)
    public static void all(final CommandEvent event) {
        event.getChannelMessage().getChannel().sendMessage("All Command");
    }

    /**
     * Replies "Pong!" if the command author is the channel's broadcaster.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "ping", level = CommandLevel.BROADCASTER)
    public static void ping(final CommandEvent event) {
        final String pongMsg = String.format("@%s Pong!", event.getChannelMessage().getAuthor().getDisplayName());
        event.getChannelMessage().getChannel().sendMessage(pongMsg);
    }

    /**
     * Should not reply at all since the command is disabled.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "disabled", aliases = {"foo"}, level = CommandLevel.DISABLED)
    public static void disabled(final CommandEvent event) {
        log.error("Error with DISABLED Command Level. This command should never be triggered");
        event.getChannelMessage().getChannel().sendMessage("ERROR: This command should have never been triggered.");
    }

    /**
     * Prints out a list of all commands that the user can run.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "commands", level = CommandLevel.ALL)
    public static void commands(final CommandEvent event) {
        final CommandRunner commandRunner = event.getChannelMessage().getSecurityRobot().getCommandRunner();
        final TwitchMessageAuthor author = event.getChannelMessage().getAuthor();

        final Set<Command> commandList = commandRunner.getCommandList().stream()
            .filter(c -> !c.getName().equals("commands") && c.getLevel().userCanRun(author))
            .collect(Collectors.toSet());

        final String commandString = commandList.stream()
            .map(c -> SecurityRobot.COMMAND_PREFIX + c.getName())
            .sorted()
            .collect(Collectors.joining(", "));
        event.getChannelMessage().getChannel().sendMessage(
            String.format("@%s: %s", author.getDisplayName(), commandString));
    }
}
