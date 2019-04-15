package gg.sep.securityrobot;

import static gg.sep.securityrobot.SecurityRobot.REDIS_PREFIX;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kitteh.irc.client.library.Client;
import redis.clients.jedis.Jedis;

import gg.sep.securityrobot.utils.IRCUtils;

/**
 * Wrapper around Kitteh IRC client with Security Robot specific functionality.
 */
@Log4j2
public class SecurityRobotClient {

    @Getter private Client ircClient;
    @Getter private SecurityRobot securityRobot;

    /**
     * Create a new instance wrapping the specified Kitteh IRC client.
     * @param bot The base SecurityRobot instance which holds this client.
     * @param ircClient Kitteh IRC client to wrap.
     */
    public SecurityRobotClient(final SecurityRobot bot, final Client ircClient) {
        this.securityRobot = bot;
        this.ircClient = ircClient;
    }

    /**
     * Joins a Twitch IRC channel and adds the channel to the initial channel list.
     * @param channel Twitch channel in either IRC or non-IRC form.
     */
    public void joinChannel(final String channel) {
        joinChannel(channel, true);
    }

    /**
     * Joins a Twitch IRC channel, with the option to add it to the initial channel list.
     * @param channel Twitch channel in either IRC or non-IRC form.
     * @param addToInitial If {@code true}, the channel will also be added to the initial channel list.
     */
    public void joinChannel(final String channel, final boolean addToInitial) {
        final String ircChannelName = IRCUtils.ircify(channel);
        if (addToInitial) {
            try (Jedis jedis = getSecurityRobot().getJedisPool().getResource()) {
                jedis.sadd(REDIS_PREFIX + "added_channels", IRCUtils.stripIrcChannel(channel));
            }
        }
        ircClient.addChannel(ircChannelName);
    }

    /**
     * Leaves a Twitch IRC channel. Does not remove it from it from the initial channel list.
     * @param channel Twitch channel in either IRC or non-IRC form.
     */
    public void leaveChannel(final String channel) {
        leaveChannel(channel, false);
    }

    /**
     * Leave a Twitch IRC channel, with the option to remove it from the initial channel list.
     * @param channel Twitch channel in either IRC or non-IRC form.
     * @param removeFromInitial If {@code true}, the channel will also be removed from the initial channel list.
     */
    public void leaveChannel(final String channel, final boolean removeFromInitial) {
        final String ircChannelName = IRCUtils.ircify(channel);
        if (removeFromInitial) {
            try (Jedis jedis = getSecurityRobot().getJedisPool().getResource()) {
                jedis.srem(REDIS_PREFIX + "added_channels", IRCUtils.stripIrcChannel(channel));
            }
        }
        ircClient.removeChannel(ircChannelName);
    }

    /**
     * Disconnect the Kitteh IRC client, performing any necessary shutdown tasks.
     * @param reason Reason for disconnecting.
     */
    public synchronized void disconnect(final String reason) {
        this.getIrcClient().shutdown(reason);
    }
}
