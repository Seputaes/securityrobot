package gg.sep.securityrobot;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.twitch.TwitchSupport;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import gg.sep.securityrobot.commands.CommandManager;
import gg.sep.securityrobot.config.ConfigLoader;
import gg.sep.securityrobot.config.models.ApplicationConfig;
import gg.sep.securityrobot.config.models.RedisConfig;
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
    public static final String REDIS_PREFIX = "securityrobot:";
    public static final String COMMAND_PREFIX = "+";
    @Getter private final ApplicationConfig config;
    @Getter private SecurityRobotClient securityRobotClient;
    @Getter private TwitchAPI twitchAPI;
    @Getter private MongoWrapper mongoWrapper;
    @Getter private CommandManager commandManager;
    @Getter private JedisPool jedisPool;

    /**
     * Creates a new instance of SecurityRobot and loads all instance variables.
     */
    public SecurityRobot() {
        this.config = ConfigLoader.loadConfig();

        initTwitchAPI();
        this.mongoWrapper = new MongoWrapper(this.config.getMongodb());
        this.jedisPool = initJedisPool();
    }

    /**
     * Starts running the bot, connects to the IRC server, joins channels, and adds listeners.
     * @throws SecurityRobotFatal Exception indicating that the bot cannot proceed and will shut down.
     */
    public void start() throws SecurityRobotFatal {
        this.securityRobotClient = new SecurityRobotClient(this, buildIrcClient());
        addListeners();
        joinInitialChannels();
        this.commandManager = new CommandManager(this);
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
     */
    private void joinInitialChannels() {
        final Set<String> initialChannels = new HashSet<>();
        initialChannels.add(IRCUtils.ircify((getConfig().getTwitch().getIrcNickname()))); // always join the bots own channel

        try (Jedis jedis = this.jedisPool.getResource()) {
            final Set<String> activeChannels = jedis.smembers(REDIS_PREFIX + "added_channels");
            activeChannels.forEach(c -> initialChannels.add(IRCUtils.ircify(c)));
        }
        initialChannels.forEach(c -> securityRobotClient.joinChannel(c, false));
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

    private JedisPool initJedisPool() {
        final int defaultJedisTimeout = 2000;
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        final RedisConfig redisConfig = this.getConfig().getRedis();
        return new JedisPool(poolConfig, redisConfig.getHost(), redisConfig.getPort(),
            defaultJedisTimeout, redisConfig.getPassword());
    }

    /**
     * Disconnects the IRC bot and shuts down any running processes.
     * @param reason Reason message for shutting down the bot.
     */
    public synchronized void shutdown(final String reason) {
        this.securityRobotClient.disconnect(reason);
    }
}
