package gg.sep.securityrobot.models.twitch.tmi;

import java.util.List;

import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.feature.twitch.messagetag.Badges;

import gg.sep.securityrobot.commands.CommandLevel;

/**
 * Represents an IRC user that is tied to a specific Twitch IRC channel.
 */
public interface TwitchChannelUser {

    /**
     * Returns the Kitteh IRC {@link Channel} object associated with the user.
     * @return Kitteh IRC channel.
     */
    Channel getChannel();

    /**
     * Returns <code>true</code> if the user is a moderator in the channel.
     *
     * @return <code>true</code> if the user is a moderator in the channel;
     *         <code>false</code> otherwise
     */
    boolean isMod();

    /**
     * Returns <code>true</code> if the user is a subscriber of the channel.
     *
     * @return <code>true</code> if the user is a subscriber of the channel;
     *         <code>false</code> otherwise
     */
    boolean isSub();

    /**
     * Returns all of the Twitch badges associated with the user.
     * @return The Twitch badges associated with the user.
     */
    List<Badges.Badge> getBadges();

    /**
     * Returns the user-selected chat name color for the user.
     * @return The user-selected chat name color for the user, formatted as a hex/HTML,
     *         eg #80FF12.
     */
    String getColor();

    /**
     * Returns <code>true</code> if the user is also the broadcaster of the channel.
     * @return <code>true></code> if the user is also the broadcaster of the channel,
     *         <code>false</code> otherwise.
     */
    boolean isBroadcaster();

    /**
     * Returns <code>true</code> if the user is a follower of the channel.
     * @return <code>true></code> if the user is a follower of the channel,
     *         <code>false</code> otherwise.
     */
    boolean isFollower();

    /**
     * Returns <code>true</code> if the twitch user has can run a specific command level.
     * @param level Level associated with the command.
     * @return <code>true</code> if the user can run a specified command level;
     *         <code>false</code> otherwise.
     */
    boolean canRunCommandLevel(CommandLevel level);

    /**
     * Returns the command level associated with the user to determine if the user can run a command.
     * @return The command level associated with a user.
     *         Users can run commands at or below their current level.
     */
    double getCommandLevel();
}
