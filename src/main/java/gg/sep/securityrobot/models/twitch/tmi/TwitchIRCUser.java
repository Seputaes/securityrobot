package gg.sep.securityrobot.models.twitch.tmi;

/**
 * Represents a global Twitch Chat User, with attributes shared across all instances of the user.
 */
public interface TwitchIRCUser {

    /**
     * Returns the Twitch User ID associated with the user.
     * @return The Twitch User ID associated with the user.
     */
    String getUserId();

    /**
     * Returns the Twitch User Name (login name) associated with the user.
     * @return The Twitch User Name (login name) associated with the user.
     */
    String getUserName();

    /**
     * Returns the user-selected Display Name associated with the user.
     * @return The user-selected Display Name associated with the user.
     */
    String getDisplayName();

    /**
     * Returns <code>true</code> if the user is a Twitch Turbo member.
     * @return <code>true</code> if the user is a Twitch Turbo member,
     *         <code>false</code> otherwise.
     */
    boolean isTurbo();

    /**
     * Returns the user's {@link TwitchIRCUser#getDisplayName()} prefixed with '@' so they are mentioned.
     * @return Returns the user's display name prefixed with '@' so they are mentioned.
     */
    String getMention();
}
