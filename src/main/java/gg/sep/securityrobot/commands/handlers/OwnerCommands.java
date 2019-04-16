package gg.sep.securityrobot.commands.handlers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.commands.ChatCommand;
import gg.sep.securityrobot.commands.CommandEvent;
import gg.sep.securityrobot.commands.CommandLevel;
import gg.sep.securityrobot.utils.CommandUtils;
import gg.sep.twitchapi.helix.model.user.User;

/**
 * Bot commands reserved for the Bot Owner. :)
 */
@Log4j2
@UtilityClass
public class OwnerCommands {

    /**
     * Beep boop.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "owner", level = CommandLevel.BOT_OWNER, showInCommandList = false)
    public static void owner(final CommandEvent event) {
        event.mention("Beep boop");
    }

    /**
     * Temporarily join a specified twitch channel, until the next bot restart.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "tjoin", level = CommandLevel.BOT_OWNER, showInCommandList = false)
    public static void tjoin(final CommandEvent event) {
        botOwnerJoinLeave(event, true, false);
    }

    /**
     * Permanently join a specified twitch channel, so that it is added to the initial channel list.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "pjoin", level = CommandLevel.BOT_OWNER, showInCommandList = false)
    public static void pjoin(final CommandEvent event) {
        botOwnerJoinLeave(event, true, true);
    }

    /**
     * Temporarily leave a specified twitch channel, until the next bot restart.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "tleave", level = CommandLevel.BOT_OWNER, showInCommandList = false)
    public static void tleave(final CommandEvent event) {
        botOwnerJoinLeave(event, false, false);
    }

    /**
     * Permanently leave a specified twitch channel, so that it is removed from the initial channel list.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "pleave", level = CommandLevel.BOT_OWNER, showInCommandList = false)
    public static void pleave(final CommandEvent event) {
        botOwnerJoinLeave(event, false, true);
    }

    private static void botOwnerJoinLeave(final CommandEvent event, final boolean isJoin, final boolean modifyInitial) {
        final Optional<String> channel = event.getCommandText();

        if (channel.isPresent() && channel.get().trim().split(" ").length == 1) {
            // make sure it's a valid user
            final String userLogin = channel.get().trim().toLowerCase();
            final Optional<User> user = event.getSecurityRobot().getTwitchAPI().getHelix()
                .getUsersAPI().getUserByLogin(userLogin);

            if (user.isPresent()) {
                if (isJoin) {
                    event.getSecurityRobot().getSecurityRobotClient().joinChannel(user.get().getLogin(), modifyInitial);
                    event.mention(String.format("Joining channel: %s", userLogin));
                } else {
                    event.getSecurityRobot().getSecurityRobotClient().leaveChannel(user.get().getLogin(), modifyInitial);
                    event.mention(String.format("Leaving channel: %s", userLogin));
                }
            } else {
                event.errorMention("That user was not identified as a valid channel/user.");
            }
        }
    }

    /**
     * Removes a global custom command.
     *
     * Syntax: [p]globalcom del {commandName}
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "globalcom del", aliases = "deletethisshit", level = CommandLevel.BOT_OWNER, showInCommandList = false)
    public static void globalcomDel(final CommandEvent event) {
        final List<String> commandParts = event.getCommandText()
            .map(c -> CommandUtils.splitString(c, 1))
            .orElse(Collections.emptyList());
        if (commandParts.size() < 1) {
            event.errorMention("Invalid format for command delete.");
            return;
        }
        final String commandName = commandParts.get(0);

        // check if the custom command exists
        if (!event.getCommandManager().commandExists(commandName, true)) {
            event.errorMention("Did not find a global custom command: " + commandName);
            return;
        }

        event.getSecurityRobot().getCommandManager()
            .delCustomCommand(commandName);
        event.successMention("Removed global command: " + commandName);
    }

    /**
     * Add a global custom command.
     *
     * Syntax: [p]globalcom add {commandName} {response string}
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "globalcom add", level = CommandLevel.BOT_OWNER, showInCommandList = false)
    public static void globalcomAdd(final CommandEvent event) {
        final List<String> commandResponse = event.getCommandText()
            .map(c -> CommandUtils.splitString(c, 2))
            .orElse(Collections.emptyList());

        if (commandResponse.size() < 2) {
            event.errorMention("Invalid format for command add.");
            return;
        }
        final String commandName = commandResponse.get(0);
        final String response = commandResponse.get(1);

        // check if the command exists
        if (event.getCommandManager().commandExists(commandName, false)) {
            event.errorMention("A global with that name or alias already exists");
            return;
        }

        event.getSecurityRobot().getCommandManager()
            .addCustomCommand(commandName, response, CommandLevel.ALL.getLevel());
        event.successMention("Added global command: " + commandName);
    }
}
