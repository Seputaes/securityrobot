package gg.sep.securityrobot;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kitteh.irc.client.library.Client;

/**
 * Wrapper around Kitteh IRC client with Security Robot specific functionality.
 */
@Log4j2
public class SecurityRobotClient {

    @Getter private Client ircClient;

    /**
     * Create a new instance wrapping the specified Kitteh IRC client.
     * @param ircClient Kitteh IRC client to wrap.
     */
    public SecurityRobotClient(final Client ircClient) {
        this.ircClient = ircClient;
    }

    /**
     * Disconnect the Kitteh IRC client, performing any necessary shutdown tasks.
     * @param reason Reason for disconnecting.
     */
    public synchronized void disconnect(final String reason) {
        this.getIrcClient().shutdown(reason);
    }
}
