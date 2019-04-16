package gg.sep.securityrobot.commands;

import java.util.Optional;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelUser;

/**
 * Which chatters can execute a command.
 * For future scaling, it might be better to implement this with bit fields and an EnumSet.
 */
@Log4j2
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
     * Parses a command's required level string (probably from a database) into the associated CommandLevel.
     *
     * If the parsing fails, or it is not found, DISABLED is returned to be safe.
     * @param levelDouble String value of the level's double value.
     * @return Command Level of the string if found, otherwise DISABLED.
     */
    public static CommandLevel parseRequiredLevelString(final String levelDouble) {
        return parseDoubleString(levelDouble).orElse(CommandLevel.DISABLED);
    }

    /**
     * Parses a users's command level string (probably from a database) into the associated CommandLevel.
     *
     * If the parsing fails, or it is not found, ALL is returned to be safe.
     * @param levelDouble String value of the level's double value.
     * @return Command Level of the string if found, otherwise ALL.
     */
    public static CommandLevel parseUserLevelString(final String levelDouble) {
        return parseDoubleString(levelDouble).orElse(CommandLevel.ALL);
    }

    private static Optional<CommandLevel> parseDoubleString(final String levelDouble) {
        try {
            final Double d = Double.parseDouble(levelDouble);
            for (final CommandLevel cl : CommandLevel.values()) {
                if (d.equals(cl.getLevel())) {
                    return Optional.of(cl);
                }
            }
        } catch (final NumberFormatException e) {
            log.error("Error parsing double string into a double. String value: {}", levelDouble);
        }
        return Optional.empty();
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
     * Checks if a user can run a specified level's value.
     * @param user User to check.
     * @param levelValue Value of the level you want to check against.
     * @return {@code true} if the user's level is higher or equal to the level;
     *         {@code false} otherwise.
     */
    public static boolean userCanRunLevel(final TwitchChannelUser user, final double levelValue) {
        return user.getCommandLevel() >= levelValue;
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
