package gg.sep.securityrobot.commands;

import lombok.Getter;

import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelUser;

/**
 * Which chatters can execute a command.
 * For future scaling, it might be better to implement this with bit fields and an EnumSet.
 */
public enum CommandLevel {
    ALL(Double.NEGATIVE_INFINITY),
    FOLLOWER(100),
    SUB(200),
    MOD(300),
    BROADCASTER(9001),
    BOT_OWNER(Double.MAX_VALUE),
    DISABLED(Double.POSITIVE_INFINITY);

    @Getter
    private final double level;

    CommandLevel(final double level) {
        this.level = level;
    }

    /**
     * Returns <code>true</code> if the user is at or above this command level.
     *
     * @param user Twitch Channel User who issued the command.
     * @return <code>true</code> if the user is at or above this command level
     *         (and thus can run the command); <code>false</code> otherwise.
     */
    public boolean userCanRun(final TwitchChannelUser user) {
        return user.canRunCommandLevel(this);
    }

    /**
     * Returns the double value of a channel user's level depending on whether they are a sub, follower, etc.
     *
     * Performs an API call to check if the user is a follower of the channel.
     * @param user Twitch channel user.
     * @return The double value of a channel user's level.
     */
    public static double getCommandLevel(final TwitchChannelUser user) {
        if (user.isBotOwner()) {
            return CommandLevel.BOT_OWNER.getLevel();
        } else if (user.isBroadcaster()) {
            return CommandLevel.BROADCASTER.getLevel();
        } else if (user.isMod()) {
            return CommandLevel.MOD.getLevel();
        } else if (user.isSub()) {
            return CommandLevel.SUB.getLevel();

        // follower last to prevent unnecessary API calls
        } else if (user.isFollower()) {
            return CommandLevel.FOLLOWER.getLevel();
        } else {
            return Double.MIN_VALUE;
        }
    }
}
