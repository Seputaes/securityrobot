package gg.sep.securityrobot.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation for tagging methods to handle chat commands.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChatCommand {
    /**
     * Name of the command, the text after the command prefix.
     * @return Name of the command.
     */
    String value();

    /**
     * Aliases of the command name.
     * @return Aliases of the command.
     */
    String[] aliases() default {};

    /**
     * Optional description of the command, which can be used in help messages. // TODO
     * @return Description of the command.
     */
    String description() default "";

    /**
     * Permission level associated with the command. User must hold one this level or higher to execute the command.
     * @return Command level associated with the command
     */
    CommandLevel level() default CommandLevel.ALL;
}
