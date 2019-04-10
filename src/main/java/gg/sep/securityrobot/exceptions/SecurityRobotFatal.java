package gg.sep.securityrobot.exceptions;

import java.io.IOException;

/**
 * Fatal exception used to signal that the bot should terminated after it is thrown.
 */
public class SecurityRobotFatal extends IOException {

    /**
     * Construct the exception with the specified message.
     * @param message Error message for the exception.
     */
    public SecurityRobotFatal(final String message) {
        super(message);
    }
}
