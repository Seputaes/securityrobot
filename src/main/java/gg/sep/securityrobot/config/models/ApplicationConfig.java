package gg.sep.securityrobot.config.models;

import lombok.Getter;

/**
 * Model for the bot's Application Config.
 */
@Getter
public class ApplicationConfig {
    private String botOwnerId;
    private TwitchConfig twitch;
    private MongoDBConfig mongodb;
    private RedisConfig redis;
}
