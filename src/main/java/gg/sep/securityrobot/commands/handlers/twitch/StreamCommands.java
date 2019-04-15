package gg.sep.securityrobot.commands.handlers.twitch;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import lombok.experimental.UtilityClass;

import gg.sep.securityrobot.commands.ChatCommand;
import gg.sep.securityrobot.commands.CommandEvent;
import gg.sep.securityrobot.commands.CommandLevel;
import gg.sep.securityrobot.utils.TimeUtils;
import gg.sep.twitchapi.helix.model.stream.Stream;
import gg.sep.twitchapi.kraken.api.channels.ChannelsAPI;
import gg.sep.twitchapi.kraken.model.channel.Channel;
import gg.sep.twitchapi.kraken.model.user.User;

/**
 * Commands for stream info and managing a stream.
 */
@UtilityClass
public class StreamCommands {

    private static final DateTimeFormatter AGE_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Sets the Stream's title via the Twitch API.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "settitle", aliases = {"title"}, level = CommandLevel.MOD)
    public static void settitle(final CommandEvent event) {
        final String channelId = event.getChannelMessage().getRoomId();
        final Optional<String> title = event.getCommandText();

        final ChannelsAPI channelsAPI = event.getChannelMessage().getSecurityRobot().getTwitchAPI()
            .getKraken().getChannelsAPI();

        title.ifPresentOrElse(t -> {
            final Channel putChannel = Channel.builder()
                .id(Long.valueOf(channelId))
                .status(t).build();

            final Optional<Channel> channel = channelsAPI.updateChannel(putChannel);

            channel.ifPresentOrElse(c ->
                    event.mention(String.format("Title set to: %s", t)),
                () -> event.mention("An error occurred while updating the title"));
        }, () -> event.mention("Title must be provided"));
    }

    /**
     * Sets the Stream's game via the Twitch API.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "setgame", level = CommandLevel.MOD)
    public static void setgame(final CommandEvent event) {
        final String channelId = event.getChannelMessage().getRoomId();
        final Optional<String> game = event.getCommandText();

        final ChannelsAPI channelsAPI = event.getChannelMessage().getSecurityRobot().getTwitchAPI()
            .getKraken().getChannelsAPI();

        game.ifPresentOrElse(g -> {
            final Channel putChannel = Channel.builder()
                .id(Long.valueOf(channelId))
                .game(g).build();

            final Optional<Channel> channel = channelsAPI.updateChannel(putChannel);

            channel.ifPresentOrElse(c ->
                    event.mention(String.format("Game set to: %s", g)),
                () -> event.mention("An error occurred while updating the game."));
        }, () -> event.mention("Game name must be provided"));
    }

    /**
     * Responds with the the uptime of the currently live stream, or "currently not live" if it is offline.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "uptime", level = CommandLevel.ALL, cooldown = 20)
    public static void uptime(final CommandEvent event) {
        final String channelId = event.getChannelMessage().getRoomId();
        final Optional<Stream> stream = event.getSecurityRobot().getTwitchAPI().getHelix().getStreamsAPI()
            .getStreamByUserId(channelId);

        if (stream.isEmpty()) {
            event.mention(event.getChannelMessage().getCleanChannelName() + " is not currently live.");
            return;
        }
        event.mention("The stream has been live for " + TimeUtils.uptimeString(stream.get().getStartedAt()));
    }

    /**
     * Responds with the number of viewers of the currently live stream, or "currently not live" if it is offline.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "viewers", aliases = {"views"}, level = CommandLevel.ALL, cooldown = 20)
    public static void viewers(final CommandEvent event) {
        final String channelId = event.getChannelMessage().getRoomId();
        final Optional<Stream> stream = event.getSecurityRobot().getTwitchAPI().getHelix().getStreamsAPI()
            .getStreamByUserId(channelId);

        if (stream.isEmpty()) {
            event.mention(event.getChannelMessage().getCleanChannelName() + " is not currently live.");
            return;
        }
        event.mention(String.format("There are currently %s viewers of the stream", stream.get().getViewerCount()));
    }

    /**
     * Responds with the account age of the current channel.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "age", aliases = {"channelage"}, level = CommandLevel.ALL, cooldown = 60)
    public static void age(final CommandEvent event) {
        final String channelId = event.getChannelMessage().getRoomId();
        final Optional<User> user = event.getSecurityRobot().getTwitchAPI().getKraken()
            .getUsersAPI().getUser(channelId);
        if (user.isPresent()) {
            final ZonedDateTime createdAt = user.get().getCreatedAt();
            final String ageString = TimeUtils.uptimeString(createdAt);
            event.mention(String.format("This channel was created %s ago (%s)", ageString,
                createdAt.format(AGE_DTF)));
        }
    }

    /**
     * Responds with the account age of the user who entered the command.
     * @param event Command event which triggered the command.
     */
    @ChatCommand(value = "myage", level = CommandLevel.ALL, cooldown = 3)
    public static void myage(final CommandEvent event) {
        final String userId = event.getChannelMessage().getAuthor().getUserId();
        final Optional<User> user = event.getSecurityRobot().getTwitchAPI().getKraken()
            .getUsersAPI().getUser(userId);
        if (user.isPresent()) {
            final ZonedDateTime createdAt = user.get().getCreatedAt();
            final String ageString = TimeUtils.uptimeString(createdAt);
            event.mention(String.format("Your account was created %s ago (%s)", ageString,
                createdAt.format(AGE_DTF)));
        }
    }
}
