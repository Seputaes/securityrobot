package gg.sep.securityrobot.exceptions;

import gg.sep.securityrobot.commands.CommandManager;

/**
 * Fatal exception thrown when the {@link CommandManager} detects duplicate commands.
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
