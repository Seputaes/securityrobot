package gg.sep.securityrobot.commands.handlers.twitch;

import java.text.MessageFormat;
import java.util.Optional;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.commands.ChatCommand;
import gg.sep.securityrobot.commands.CommandEvent;
import gg.sep.securityrobot.commands.CommandLevel;
import gg.sep.twitchapi.helix.model.user.User;

/**
 * Collection of commands that interact with the Twitch API.
 */
@Log4j2
@UtilityClass
public class TwitchCommands {

    /**
     * Requests the bot join the requesting user's channel.
     *
     * The channel will be added to the initial channels list, so the bot will rejoin it on restart.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "joinme", level = CommandLevel.ALL, showInCommandList = false)
    public static void joinme(final CommandEvent event) {
        final String userId = event.getChannelMessage().getAuthor().getUserId();
        final Optional<User> user = event.getSecurityRobot().getTwitchAPI().getHelix().getUsersAPI()
            .getUserById(userId);

        if (user.isPresent()) {
            event.getSecurityRobot().getSecurityRobotClient().joinChannel(user.get().getLogin());
            event.mention("Sure thing! See you over in your channel! 🤖");
        }
    }

    /**
     * Requests that the bot leave the requesting user's channel.
     *
     * The channel will be removed from the initial channels lists, so the bot will NOT rejoin on restart.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "leaveme", level = CommandLevel.ALL, showInCommandList = false)
    public static void leaveme(final CommandEvent event) {
        final String userId = event.getChannelMessage().getAuthor().getUserId();
        final Optional<User> user = event.getSecurityRobot().getTwitchAPI().getHelix().getUsersAPI()
            .getUserById(userId);

        if (user.isPresent()) {
            event.getSecurityRobot().getSecurityRobotClient().leaveChannel(user.get().getLogin(), true);
            event.mention("It's not you, it's me ... 💔");
        }
    }

    /**
     * Responds with the number of followers of the current channel.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "followers", level = CommandLevel.MOD)
    public static void followers(final CommandEvent event) {
        final String roomId = event.getChannelMessage().getRoomId();
        final long followerCount = event.getChannelMessage().getSecurityRobot().getTwitchAPI()
            .getHelix().getUsersAPI().getFollowsAPI().getFollowerCount(roomId);
        event.mention(MessageFormat.format("The channel has {0} followers.", followerCount));
    }
}
