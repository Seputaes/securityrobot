package gg.sep.securityrobot.commands;

import lombok.Getter;

import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;

/**
 * A Chat command event, which holds both the name of the command and the chat message which triggered it.
 */
public class CommandEvent {

    @Getter private String name;
    @Getter private TwitchChannelMessage channelMessage;

    /**
     * Construct the Command Event with the name of the command and the Twitch chat message which triggered the command.
     * @param name Name of the command (text after the prefix)
     * @param message Twitch Chat message which triggered the command.
     */
    public CommandEvent(final String name, final TwitchChannelMessage message) {
        this.name = name;
        this.channelMessage = message;
    }
}
