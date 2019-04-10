package gg.sep.securityrobot;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.twitch.TwitchSupport;

import gg.sep.securityrobot.commands.CommandRunner;
import gg.sep.securityrobot.config.ConfigLoader;
import gg.sep.securityrobot.config.models.ApplicationConfig;
import gg.sep.securityrobot.db.MongoWrapper;
import gg.sep.securityrobot.exceptions.SecurityRobotFatal;
import gg.sep.securityrobot.listeners.CommandListener;
import gg.sep.securityrobot.listeners.JoinPartListener;
import gg.sep.securityrobot.listeners.LoggingListener;
import gg.sep.securityrobot.utils.IRCUtils;
import gg.sep.twitchapi.TwitchAPI;

/**
 * Main entry into the SecurityRobot bot application.
 */
@Log4j2
public class SecurityRobot {
    public static final String COMMAND_PREFIX = "+";
    @Getter private final ApplicationConfig config;
    @Getter private SecurityRobotClient securityRobotClient;
    @Getter private TwitchAPI twitchAPI;
    @Getter private MongoWrapper mongoWrapper;
    @Getter private CommandRunner commandRunner;

    /**
     * Creates a new instance of SecurityRobot and loads all instance variables.
     */
    public SecurityRobot() {
        this.config = ConfigLoader.loadConfig();

        initTwitchAPI();
        this.mongoWrapper = new MongoWrapper(this.config.getMongodb());
    }

    /**
     * Starts running the bot, connects to the IRC server, joins channels, and adds listeners.
     * @throws SecurityRobotFatal Exception indicating that the bot cannot proceed and will shut down.
     */
    public void start() throws SecurityRobotFatal {
        this.securityRobotClient = new SecurityRobotClient(buildIrcClient());
        addListeners();
        joinInitialChannels(this.securityRobotClient.getIrcClient());
        this.commandRunner = new CommandRunner(this);
    }

    /**
     * Build an instance of a Kitteh IRC client and add Twitch support. Connects to the server.
     * @return New instance of a Kitteh IRC client.
     */
    private Client buildIrcClient() {
        final Client client = Client.builder()
            .server().host(config.getTwitch().getIrcHost()).port(config.getTwitch().getIrcPort())
            .password(config.getTwitch().getIrcOauthPassword()).then()
            .nick(config.getTwitch().getIrcNickname())
            .build();
        TwitchSupport.addSupport(client);
        client.connect();
        return client;
    }

    /**
     * Initializes the Twitch API using the Twitch configuration.
     */
    private void initTwitchAPI() {
        this.twitchAPI = new TwitchAPI(this.config.getTwitch().buildAPIConfig());
    }

    /**
     * Joints all initial channels.
     * @param ircClient IRC client on which to join the channels.
     */
    private void joinInitialChannels(final Client ircClient) {
        ircClient.addChannel(IRCUtils.ircify(this.getConfig().getTwitch().getStreamChannel()));
        ircClient.addChannel("#securityrobot");
    }

    /**
     * Adds all default event listeners to the IRC client.
     */
    private void addListeners() {
        securityRobotClient.getIrcClient().getEventManager()
            .registerEventListener(new CommandListener(this));

        securityRobotClient.getIrcClient().getEventManager()
            .registerEventListener(new JoinPartListener());

        securityRobotClient.getIrcClient().getEventManager()
            .registerEventListener(new LoggingListener(this, this.mongoWrapper.getMongoClient().getDatabase("beastielogs")));
    }

    /**
     * Disconnects the IRC bot and shuts down any running processes.
     * @param reason Reason message for shutting down the bot.
     */
    public synchronized void shutdown(final String reason) {
        this.securityRobotClient.disconnect(reason);
    }
}
