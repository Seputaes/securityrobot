package gg.sep.securityrobot.models.twitch.tmi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.feature.twitch.messagetag.Badges;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.commands.CommandLevel;

/**
 * Representation of the author of a Twitch Chat message.
 *
 * This author shares both the standard Twitch IRC user attributes of {@link TwitchIRCUser}, as
 * well as the channel-specific items on {@link TwitchChannelUser}.
 */
@Log4j2
@Getter
public final class TwitchMessageAuthor implements TwitchIRCUser, TwitchChannelUser {

    // IRC User fields
    private String userId;
    private String userName;
    private String displayName;
    private boolean isTurbo;

    // Channel User fields
    private Channel channel;
    private boolean isMod;
    private boolean isSub;
    private List<Badges.Badge> badges;
    private String color;

    private TwitchChannelMessage channelMessage;
    private SecurityRobot securityRobot;

    /**
     * Construct the Message Author from the specified Twitch Channel message.
     * @param channelMessage Twitch Channel Chat message.
     */
    public TwitchMessageAuthor(final TwitchChannelMessage channelMessage) {

        final Map<String, String> tagMap = channelMessage.getTagMap();
        this.channelMessage = channelMessage;
        this.securityRobot = channelMessage.getSecurityRobot();
        this.userId = tagMap.get("user-id");
        this.userName = channelMessage.getEvent().getActor().getNick();
        this.displayName = tagMap.get("display-name");
        this.isTurbo = "1".equals(tagMap.get("turbo"));
        this.isMod = "1".equals(tagMap.get("mod"));
        this.isSub = "1".equals(tagMap.get("subscriber"));
        this.channel = channelMessage.getChannel();
        this.color = tagMap.get("color");  // TODO: Implement this as non-string;

        this.badges = parseBadges(channelMessage.getServerMessage());
    }

    /**
     * Returns <code>true</code> if the author is also the channel's broadcaster.
     * This condition is true if the author's user ID matches the channel's room ID.
     * @return <code>true</code> if the author is also the channel's broadcaster;
     *         <code>false</code> otherwise.
     */
    public boolean isBroadcaster() {
        return this.getChannelMessage().getRoomId().equals(this.getUserId());
    }

    /**
     * Returns <code>true</code> if the author is following the channel.
     *
     * NOTE: This makes an API call to Twitch.
     *
     * @return <code>true</code> if the author is following the channel;
     *         <code>false</code> otherwise.
     */
    public boolean isFollower() {
        return channelMessage.getSecurityRobot().getTwitchAPI().getUsersAPI()
            .userIsFollowing(this.getUserId(), channelMessage.getRoomId());
    }

    /**
     * Returns <code>true</code> if the author is at or above a the specified command level.
     * @param level Level associated with the command.
     * @return <code>true</code> if the author is at or above the specified command level;
     *         <code>false</code> otherwise.
     */
    public boolean canRunCommandLevel(final CommandLevel level) {
        return getCommandLevel() >= level.getLevel();
    }

    /**
     * Returns the double value associated with this user's command level.
     * @return Double value associated with this user's command level.
     */
    public double getCommandLevel() {
        return CommandLevel.getCommandLevel(this);
    }

    private List<Badges.Badge> parseBadges(final ServerMessage serverMessage) {
        final Optional<MessageTag> badgeTag = serverMessage.getTag("badges");

        return badgeTag.map(messageTag -> ((Badges) messageTag).getBadges()).orElse(Collections.emptyList());
    }
}
