package gg.sep.securityrobot.listeners;

import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent;

/**
 * IRC Event listener for handling join/part IRC events.
 * Listens to {@link ChannelJoinEvent} and {@link ChannelPartEvent} events.
 */
public class JoinPartListener {

    /**
     * Receives all channel join events and handles them accordingly.
     * @param event Raw Kitteh channel join event.
     */
    @Handler
    public void joinEvent(final ChannelJoinEvent event) {
        System.out.println(String.format("Channel Join: %s, User: %s", event.getChannel().getName(), event.getUser().getNick()));
    }

    /**
     * Receives all channel part events and handles them accordingly.
     * @param event Raw Kitteh channel part event.
     */
    @Handler
    public void leaveEvent(final ChannelPartEvent event) {
        System.out.println(String.format("Channel Leave: %s, User: %s", event.getChannel().getName(), event.getUser().getNick()));
    }
}
