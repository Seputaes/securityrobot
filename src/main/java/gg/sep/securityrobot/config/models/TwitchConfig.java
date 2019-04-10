package gg.sep.securityrobot.config.models;

import lombok.Getter;

import gg.sep.twitchapi.TwitchAPI;
import gg.sep.twitchapi.TwitchAPIConfig;

/**
 * Model for holding Twitch configuration for the bot.
 */
@Getter
public class TwitchConfig {
    private String ircHost;
    private int ircPort;
    private String ircNickname;
    private String ircOauthPassword;
    private String apiClientId;
    private String apiOauthToken;
    private double apiRateLimit;
    private String streamChannel;

    /**
     * Convert the configuration into an API config appropriate for use in {@link TwitchAPI}.
     * @return Built Twitch API config appropriate for use in {@link TwitchAPI}.
     */
    public TwitchAPIConfig buildAPIConfig() {
        return TwitchAPIConfig.builder()
            .apiRateLimit(apiRateLimit)
            .clientId(apiClientId)
            .oauthToken(apiOauthToken)
            .login(ircNickname.toLowerCase())
            .build();
    }
}
