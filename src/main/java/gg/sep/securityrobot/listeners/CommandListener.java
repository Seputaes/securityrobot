package gg.sep.securityrobot.listeners;

import lombok.extern.log4j.Log4j2;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.commands.CommandManager;
import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;

/**
 * IRC Event listener for handling bot commands.
 * Listens to the {@link ChannelMessageEvent} event.
 */
@Log4j2
public class CommandListener {

    private SecurityRobot securityRobot;

    /**
     * Create a new Command Listener instance for the specified bot.
     * @param securityRobot Bot instance to be used with this command listener.
     */
    public CommandListener(final SecurityRobot securityRobot) {
        this.securityRobot = securityRobot;
    }

    /**
     * Receives all channel message events and determines whether the message has the bot's command prefix.
     *
     * If the message is prefixed, the message is sent over to the
     * {@link CommandManager#parseCommand(TwitchChannelMessage)} method to verify
     * and dispatch/invoke the command.
     * @param event Raw Kitteh channel message event.
     */
    @Handler
    public void commandTrigger(final ChannelMessageEvent event) {
        final TwitchChannelMessage message = new TwitchChannelMessage(event, securityRobot);
        System.out.println(message.getMessageTime());

        if (message.getMessage().startsWith(getPrefix())) {
            securityRobot.getCommandManager().parseCommand(message);
        }
    }

    private String getPrefix() {
        return SecurityRobot.COMMAND_PREFIX;
    }
}
