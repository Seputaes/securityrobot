package gg.sep.securityrobot.utils;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * Utility method for parsing JSON into one of our models, and handles the Json exceptions.
 */
@Log4j2
@UtilityClass
public class ModelParser {

    public static final Gson GSON = new Gson();

    /**
     * Attempts to parse the specified JSON string into the specified Model.
     *
     * Will return optional of the class instance, or empty if there was a parson error.
     * @param json JSON string for the class.
     * @param clazz Class to parse the JSON string into.
     * @param <T> The type of the desired object.
     * @return an instance of type T from the json string, or Optional empty if there was a parsing error.
     */
    public static <T> Optional<T> parseJson(final String json, final Class<T> clazz) {
        try {
            return Optional.of(GSON.fromJson(json, clazz));
        } catch (final JsonSyntaxException e) {
            log.error("Error parsing JSON into models. class={}, error={}", clazz, e.getMessage());
            return Optional.empty();
        }
    }
}
