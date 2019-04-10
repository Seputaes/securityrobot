package gg.sep.securityrobot.config.models;

import lombok.Getter;

/**
 * Model for the bot's Application Config.
 */
@Getter
public class ApplicationConfig {
    private TwitchConfig twitch;
    private MongoDBConfig mongodb;
}
