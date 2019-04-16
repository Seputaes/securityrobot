package gg.sep.securityrobot.commands;

import static gg.sep.securityrobot.SecurityRobot.COMMAND_PREFIX;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;
import gg.sep.securityrobot.utils.reply.Replies;

/**
 * A Chat command event, which holds both the name of the command and the chat message which triggered it.
 */

@AllArgsConstructor
@Getter
public class CommandEvent {
    private SecurityRobot securityRobot;
    private Command command;
    private CommandManager commandManager;
    private TwitchChannelMessage channelMessage;

    /**
     * Returns the text of the command message after the prefix + command name.
     * @return The text of the command message after the prefix + command name.
     */
    public Optional<String> getCommandText() {

        final String trimmedMsg = channelMessage.getMessage().trim();
        final String searchMsg = trimmedMsg.toLowerCase();

        // build a string out of each of the command tree branches
        for (final List<String> branch : command.getCommandTree()) {
            final String branchPrefix = COMMAND_PREFIX + String.join(" ", branch);
            if (searchMsg.startsWith(branchPrefix)) {
                return Optional.of(trimmedMsg.substring(branchPrefix.length()).trim());
            }
        }
        return Optional.empty();
    }

    /**
     * Sends a reply message to the channel which triggered the command.
     * @param message Message to send back to the channel.
     */
    public void reply(final String message) {
        channelMessage.getEvent().sendReply(message);
    }

    /**
     * Sends a reply message, mentioning the user who triggered the command, to the channel.
     * @param message Message to send back to the channel.
     */
    public void mention(final String message) {
        reply(Replies.mention(message, channelMessage.getAuthor()));
    }

    /**
     * Reply reply to the same context as the command, prefixing the {@code message} with a
     * checkmark indicating success.
     *
     * @param message Message to send back to the command context.
     */
    public void success(final String message) {
        reply(Replies.success(message));
    }

    /**
     * Reply reply to the same context as the command, prefixing the {@code message} with a
     * cross mark indicating an error or failure.
     *
     * @param message Message to send back to the command context.
     */
    public void error(final String message) {
        reply(Replies.error(message));
    }

    /**
     * Combines the functionality of {@link #success(String)} and {@link #mention(String)}.
     * @param message Message to send back to the command context.
     */
    public void successMention(final String message) {
        reply(Replies.successMention(message, channelMessage.getAuthor()));
    }

    /**
     * Combines the functionality of {@link #error(String)} and {@link #mention(String)}.
     * @param message Message to send back to the command context.
     */
    public void errorMention(final String message) {
        reply(Replies.errorMention(message, channelMessage.getAuthor()));
    }
}
