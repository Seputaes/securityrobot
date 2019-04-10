package gg.sep.securityrobot.config.models;

import lombok.Getter;

/**
 * Model for the bot's MongoDB config.
 */
@Getter
public class MongoDBConfig {
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
}
