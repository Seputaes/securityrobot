package gg.sep.securityrobot.commands;

/**
 * Interface for marking classes which handle commands.
 * If a method in a class is annotated with {@link ChatCommand}, it must also implement this interface.
 *
 * All implementations of this interface will be added as "listeners" to the command runner.
 */
public interface CommandHandler {
}
