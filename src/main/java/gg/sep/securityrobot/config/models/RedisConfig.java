package gg.sep.securityrobot.config.models;

import lombok.Getter;

/**
 * Model for the Redis configuration section of the app config file.
 */
@Getter
public class RedisConfig {
    private String host;
    private int port;
    private String password;
}
