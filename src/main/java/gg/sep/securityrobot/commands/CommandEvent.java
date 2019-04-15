package gg.sep.securityrobot.commands;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;

/**
 * A Chat command event, which holds both the name of the command and the chat message which triggered it.
 */

@AllArgsConstructor
@Getter
public class CommandEvent {
    private SecurityRobot securityRobot;
    private Command command;
    private CommandRunner commandRunner;
    private TwitchChannelMessage channelMessage;

    /**
     * Returns the text of the command message after the prefix + command name.
     * @return The text of the command message after the prefix + command name.
     */
    public Optional<String> getCommandText() {
        final String[] splitMsg = channelMessage.getMessage().trim().split(" ", 2);
        return (splitMsg.length < 2) ? Optional.empty() : Optional.of(splitMsg[1]);
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
        reply(channelMessage.getAuthor().getMention() + " " + message);
    }
}
