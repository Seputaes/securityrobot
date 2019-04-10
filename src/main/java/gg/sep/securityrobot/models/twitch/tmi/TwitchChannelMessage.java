package gg.sep.securityrobot.models.twitch.tmi;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.models.db.TwitchChannelMessageLog;
import gg.sep.securityrobot.utils.IRCUtils;

/**
 * Represents a Twitch Channel message. Wraps and parses raw IRC message events.
 */
@Log4j2
@Getter
public final class TwitchChannelMessage {
    private SecurityRobot securityRobot;

    private String id;
    private String message;
    private Channel channel;
    private String cleanChannelName;
    private String roomId;
    private TwitchMessageAuthor author;
    private ChannelMessageEvent event;
    private OffsetDateTime messageTime;
    private ServerMessage serverMessage;
    private Map<String, String> tagMap;

    /**
     * Construct a channel message object from the specified raw Kitteh channel message event and bot.
     * @param event Raw Kitteh channel message event.
     * @param securityRobot Bot instance which received the message.
     */
    public TwitchChannelMessage(final ChannelMessageEvent event, final SecurityRobot securityRobot) {
        this.securityRobot = securityRobot;
        this.serverMessage = event.getSource();
        this.tagMap = parseTagMap(this.serverMessage);
        this.id = this.tagMap.get("id");
        this.roomId = this.tagMap.get("room-id");
        this.messageTime = parseMessageTime(this.tagMap);

        this.event = event;
        this.message = event.getMessage();
        this.channel = event.getChannel();
        this.cleanChannelName = cleanChannelName(this.channel);
        this.author = new TwitchMessageAuthor(this);
    }

    /**
     * Converts the channel message event into a Log models object for storage.
     * @return Converted channel message log object.
     */
    public TwitchChannelMessageLog toLog() {
        return TwitchChannelMessageLog.builder()
            .twitchId(this.id)
            .message(this.message)
            .roomId(this.roomId)
            .messageTime(this.messageTime.toEpochSecond())
            .authorId(this.author.getUserId())
            .authorDisplayName(this.author.getDisplayName())
            .channelName(this.channel.getName())
            .cleanChannelName(this.cleanChannelName)
            .build();
    }

    private static String cleanChannelName(final Channel channel) {
        return IRCUtils.stripIrcChannel(channel.getLowerCaseName());
    }

    private static Map<String, String> parseTagMap(final ServerMessage message) {
        final Map<String, String> parsedMap = new HashMap<>();

        if (message == null) {
            return parsedMap;
        }

        for (final MessageTag tag : message.getTags()) {
            if (tag.getValue().isPresent()) {
                parsedMap.put(tag.getName(), tag.getValue().get());
            }
        }
        return parsedMap;
    }

    private static OffsetDateTime parseMessageTime(final Map<String, String> tagMap) {
        final String tmiSentTs = tagMap.get("tmi-sent-ts");
        final Instant instant = (tmiSentTs == null) ? Instant.now() : Instant.ofEpochMilli(Long.parseLong(tmiSentTs));
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
