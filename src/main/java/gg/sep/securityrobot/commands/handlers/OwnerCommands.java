package gg.sep.securityrobot.commands.handlers;

import java.util.Optional;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.commands.ChatCommand;
import gg.sep.securityrobot.commands.CommandEvent;
import gg.sep.securityrobot.commands.CommandLevel;
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
                event.mention("That user was not identified as a valid channel/user.");
            }
        }
    }

    /**
     * Add or remove a global custom command.
     *
     * Syntax: [p]globalcom add|del commandName [response string]
     * Response string is required if the action is "add."
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "globalcom", level = CommandLevel.BOT_OWNER, showInCommandList = false)
    public static void globalcom(final CommandEvent event) {
        final Optional<String> commandText = event.getCommandText();

        // check the action syntax first
        if (commandText.isEmpty() || commandText.get().split(" ", 2).length < 2) {
            event.mention("Invalid custom command format");
            return;
        }

        final String[] splitCommand = commandText.get().split(" ", 2);
        final String actionString = splitCommand[0];

        final Optional<Boolean> action = getGlobalCommandAction(actionString);

        if (action.isEmpty()) {
            event.mention(String.format("\"%s\" is not a valid action for globalcom", actionString));
            return;
        }

        if (action.get()) {
            final String[] addSplit = splitCommand[1].split(" ", 2);
            if (addSplit.length < 2) {
                event.mention("Invalid format for command add.");
                return;
            }
            final String commandName = addSplit[0];
            final String response = addSplit[1];

            event.getSecurityRobot().getCommandManager()
                .addCustomCommand(commandName, response, CommandLevel.ALL.getLevel());
            event.mention("Added command: " + commandName);

        } else {
            final String[] removeSplit = splitCommand[1].split(" ", 1);
            if (removeSplit.length < 1) {
                event.mention("Invalid format for split command");
                return;
            }
            final String commandName = removeSplit[0];
            event.getSecurityRobot().getCommandManager()
                .delCustomCommand(commandName);
            event.mention("Removed command: " + commandName);
        }
    }

    private Optional<Boolean> getGlobalCommandAction(final String action) {
        if ("add".equalsIgnoreCase(action)) {
            return Optional.of(true);
        } else if ("del".equalsIgnoreCase(action)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }
}
