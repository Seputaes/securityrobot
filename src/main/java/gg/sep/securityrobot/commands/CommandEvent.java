package gg.sep.securityrobot.commands;

import lombok.Getter;

import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;

/**
 * A Chat command event, which holds both the name of the command and the chat message which triggered it.
 */
public class CommandEvent {

    @Getter private Command command;
    @Getter private TwitchChannelMessage channelMessage;

    /**
     * Construct the Command Event with the name of the command and the Twitch chat message which triggered the command.
     * @param command The command which is tied to the message.
     * @param message Twitch Chat message which triggered the command.
     */
    public CommandEvent(final Command command, final TwitchChannelMessage message) {
        this.command = command;
        this.channelMessage = message;
    }
}
