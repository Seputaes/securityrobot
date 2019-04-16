package gg.sep.securityrobot.utils.reply;

import lombok.experimental.UtilityClass;

import gg.sep.securityrobot.models.twitch.tmi.TwitchMessageAuthor;

/**
 * Utility class for building Twitch Chat reply messages.
 */
@UtilityClass
public class Replies {

    private static final String SUCCESS_EMOJI = "✅";
    private static final String ERROR_EMOJI = "❌";

    /**
     * Prefixes the message with the user's mention: @displayName.
     * @param message Message to append to the mention.
     * @param author Author to mention.
     * @return Message prefixed with the author's mention string.
     */
    public String mention(final String message, final TwitchMessageAuthor author) {
        return author.getMention() + " " + message;
    }

    /**
     * Prefixes the message with a checkmark emoji, indicating success.
     * @param message Message to append to the success checkmark.
     * @return Message prefixed with a checkmark emoji, indicating success.
     */
    public String success(final String message) {
        return SUCCESS_EMOJI + " " + message;
    }

    /**
     * Prefixes the message with a red cross emoji, indicating error/failure.
     * @param message Message to append to the error/failure cross mark.
     * @return Message prefixed with a cross mark emoji, indicating error/failure.
     */
    public String error(final String message) {
        return ERROR_EMOJI + " " + message;
    }

    /**
     * Combines both {@link #success(String)} and {@link #mention(String, TwitchMessageAuthor)}.
     * @param message Message to append to the success checkmark and author's mention.
     * @param author Author to mention.
     * @return Message prefixed with the success checkmark and author's mention, indicating success.
     */
    public String successMention(final String message, final TwitchMessageAuthor author) {
        return success(mention(message, author));
    }

    /**
     * Combines both {@link #error(String)} and {@link #mention(String, TwitchMessageAuthor)}.
     * @param message Message to append to the error cross mark and author's mention.
     * @param author Author to mention.
     * @return Message prefixed with the error cross mark and author's mention, indicating error/failure.
     */
    public String errorMention(final String message, final TwitchMessageAuthor author) {
        return error(mention(message, author));
    }
}
