package gg.sep.securityrobot.commands.handlers;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.commands.ChatCommand;
import gg.sep.securityrobot.commands.CommandEvent;
import gg.sep.securityrobot.commands.CommandHandler;
import gg.sep.securityrobot.commands.CommandLevel;
import gg.sep.securityrobot.commands.CommandRunner;
import gg.sep.securityrobot.models.twitch.tmi.TwitchMessageAuthor;

/**
 * Collection of General commands for the bot.
 * TODO: These are work in progress and generally designed for testing while in development.
 */
@Log4j2
public class GeneralCommands implements CommandHandler {

    /**
     * All users should be able to execute this command.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "all", level = CommandLevel.ALL)
    public void all(final CommandEvent event) {
        event.getChannelMessage().getChannel().sendMessage("All Command");
    }

    /**
     * Replies "Pong!" if the command author is the channel's broadcaster.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "ping", level = CommandLevel.BROADCASTER)
    public void ping(final CommandEvent event) {
        final String pongMsg = String.format("@%s Pong!", event.getChannelMessage().getAuthor().getDisplayName());
        event.getChannelMessage().getChannel().sendMessage(pongMsg);
    }

    /**
     * Should not reply at all since the command is disabled.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "disabled", level = CommandLevel.DISABLED)
    public void disabled(final CommandEvent event) {
        log.error("Error with DISABLED Command Level. This command should never be triggered");
        event.getChannelMessage().getChannel().sendMessage("ERROR: This command should have never been triggered.");
    }

    /**
     * Prints out a list of all commands that the user can run.
     * @param event Command event constructed from the chat message.
     */
    @ChatCommand(value = "commands", level = CommandLevel.ALL)
    public void commands(final CommandEvent event) {
        final CommandRunner commandRunner = event.getChannelMessage().getSecurityRobot().getCommandRunner();
        final TwitchMessageAuthor author = event.getChannelMessage().getAuthor();

        final Set<String> commandList = new HashSet<>();
        // find all the commands that the user can run
        for (final Map.Entry<String, Pair<Method, CommandLevel>> command : commandRunner.getCommandList().entrySet()) {
            if (command.getValue().getRight().userCanRun(author)) {
                commandList.add(command.getKey());
            }
        }
        final String commandString = commandList.stream()
            .filter(c -> !c.equals("commands"))
            .map(c -> SecurityRobot.COMMAND_PREFIX + c)
            .sorted()
            .collect(Collectors.joining(", "));
        event.getChannelMessage().getChannel().sendMessage(
            String.format("@%s: %s", author.getDisplayName(), commandString));
    }
}
