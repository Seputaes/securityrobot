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
}
