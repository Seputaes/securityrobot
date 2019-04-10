package gg.sep.securityrobot.exceptions;

/**
 * Fatal exception thrown when the {@link gg.sep.securityrobot.commands.CommandRunner} detects duplicate commands.
 */
public class DuplicateCommandException extends SecurityRobotFatal {
    /**
     * Construct the exception with the specified message.
     * @param message Error message for the exception.
     */
    public DuplicateCommandException(final String message) {
        super(message);
    }
}
