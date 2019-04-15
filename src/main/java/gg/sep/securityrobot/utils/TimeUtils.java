package gg.sep.securityrobot.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import lombok.experimental.UtilityClass;

/**
 * A collection of utilities for working with times and dates.
 */
@UtilityClass
public class TimeUtils {

    /**
     * Gets the difference in time between two Temporals and outputs a
     * formatted "years, days, hours, minutes, seconds" string.
     * @param date1 The first temporal.
     * @param date2 The second temporal.
     * @return A string in the format of "0 years, 0 days, 0 hours, 0 minutes, 0 seconds"
     *         If years/days are 0, they will be omitted.
     */
    public static String uptimeString(final Temporal date1, final Temporal date2) {
        final long msUptime = ChronoUnit.MILLIS.between(date1, date2);
        return msToYDHMS(msUptime);
    }

    /**
     * Same as {@link #uptimeString(Temporal, Temporal)}, but uses UTC Now as one of the dates.
     * @param pastDate The temporal to compare to now.
     * @return A string in the format of "0 years, 0 days, 0 hours, 0 minutes, 0 seconds"
     *         If years/days are 0, they will be omitted.
     */
    public static String uptimeString(final Temporal pastDate) {
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        return uptimeString(pastDate, now);
    }

    /**
     * Converts milliseconds into a formatted string of "0 years, 0 days, 0 hours, 0 minutes, 0 seconds".
     * @param timeMs Number of milliseconds between two dates.
     * @return A string in the format of "0 years, 0 days, 0 hours, 0 minutes, 0 seconds"
     *         If years/days are 0, they will be omitted.
     */
    public static String msToYDHMS(final long timeMs) {
        final long seconds = (int) (timeMs / 1000) % 60;
        final long minutes = (int) ((timeMs / (1000 * 60)) % 60);
        final long hours = (int) ((timeMs / (1000 * 60 * 60)) % 24);
        final long days = (int) ((timeMs / (1000 * 60 * 60 * 24)) % 365);

        // numeric overflow
        final long hoursDiv = (1000 * 60 * 60 * 24);
        final long yearsDiv = (hoursDiv * 365);
        final long years = (int) ((timeMs / yearsDiv));

        final StringBuilder sb = new StringBuilder();

        if (years > 0) {
            sb.append(years).append(" ").append(pluralizer("year", years)).append(", ");
        }
        if (days > 0) {
            sb.append(days).append(" ").append(pluralizer("day", years)).append(", ");
        }
        if (hours > 0) {
            sb.append(hours).append(" ").append(pluralizer("hour", hours)).append(", ");
        }
        sb.append(minutes).append(" ").append(pluralizer("minute", minutes)).append(", ");
        sb.append(hours).append(" ").append(pluralizer("second", seconds));
        return sb.toString();
    }

    private static String pluralizer(final String singular, final long count) {
        return (count == 1) ? singular : singular + "s";
    }
}
