package gg.sep.securityrobot.utils;

import java.util.Arrays;
import java.util.List;

import lombok.experimental.UtilityClass;

/**
 * Utilities for parsing and working with chat commands.
 */
@UtilityClass
public class CommandUtils {

    /**
     * Shorthand for {@link String#split(String, int)} by space that converts the array to a List.
     * @param original Original string to split.
     * @param max Maximum number of items in the resulting list.
     * @return List of strings, which were split by space from the original string.
     */
    public static List<String> splitString(final String original, final int max) {
        return Arrays.asList(original.split(" ", max));
    }
}
