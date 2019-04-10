package gg.sep.securityrobot.utils;

import lombok.experimental.UtilityClass;

/**
 * Collection of utilities for working with IRC.
 */
@UtilityClass
public class IRCUtils {

    /**
     * Cleans an IRC channel name (in the format of #example).
     *
     * Removes the first hash and lowercases.
     * @param channelName IRC channel name.
     * @return Cleaned IRC channel name.
     */
    public static String stripIrcChannel(final String channelName) {
        if (channelName.startsWith("#")) {
            return channelName.replaceFirst("#", "").toLowerCase();
        }
        return channelName.toLowerCase();
    }

    /**
     * Converts a cleaned IRC channel name into one that can be used to join/part IRC channels.
     *
     * Performs the reverse of {@link IRCUtils#stripIrcChannel(String)} (minus lowercase reversal).
     * @param cleanName Clean name of the channel.
     * @return IRC channel name appropriate for joining/parting channels and for use in IRC commands.
     */
    public static String ircify(final String cleanName) {
        if (cleanName.startsWith("#")) {
            return cleanName.toLowerCase();
        }
        return "#" + cleanName.toLowerCase();
    }
}
