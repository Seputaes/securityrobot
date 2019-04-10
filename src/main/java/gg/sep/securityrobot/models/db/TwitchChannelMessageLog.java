package gg.sep.securityrobot.models.db;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Model for Twitch channel chat messages, for use in storage.
 */
@Builder
@Getter
public class TwitchChannelMessageLog {
    @NonNull private String twitchId;
    @NonNull private String message;
    @NonNull private String roomId;
    @NonNull private Long messageTime;
    @NonNull private String authorId;
    @NonNull private String authorDisplayName;
    @NonNull private String channelName;
    @NonNull private String cleanChannelName;

    /**
     * Converts the object to a JSON string.
     * @return JSON string representation of the object.
     */
    public String toJson() {
        return new Gson().toJson(this);
    }
}
