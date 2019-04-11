package gg.sep.securityrobot.commands;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Objects;
import lombok.Builder;
import lombok.Getter;

/**
 * Model of a bot command with builder.
 */
@Builder
@Getter
public class Command {

    private String name;
    private String description;
    private Set<String> aliases;
    private Method method;
    private CommandLevel level;

    /**
     * Builder class for the command, with custom alias handling.
     */
    public static class CommandBuilder {
        /**
         * Converts a string array to a set and returns the builder.
         * @param aliases String array of aliases.
         * @return The builder instance.
         */
        public CommandBuilder aliases(final String[] aliases) {
            this.aliases = Stream.of(aliases)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            return this;
        }

        /**
         * Converts a collection of strings to a set and returns the builder.
         * @param aliases Collection of string aliases.
         * @return The builder instance.
         */
        public CommandBuilder aliases(final Collection<String> aliases) {
            this.aliases = aliases.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            return this;
        }

        /**
         * Same behavior as {@link CommandBuilder#aliases(Collection)}.
         * @param aliases Set of string aliases.
         * @return The builder instance.
         */
        public CommandBuilder aliases(final Set<String> aliases) {
            return this.aliases((Collection<String>) aliases);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Command)) {
            return false;
        }
        final Command o = (Command) other;
        return o.getName().equals(this.getName()) && o.getAliases().equals(this.getAliases());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.getName(), this.getAliases());
    }

    /**
     * Returns <code>true</code> if this Command handles the extracted command string from a Twitch Message.
     * @param commandStr Extracted command string (without the prefix) in the message.
     * @return <code>true</code> if this Command handles the extracted command string;
     *         <code>false</code> otherwise.
     */
    public boolean handlesCommand(final String commandStr) {
        return this.name.equalsIgnoreCase(commandStr) || this.aliases.stream().anyMatch(commandStr::equalsIgnoreCase);
    }

    /**
     * Builds a Command from a {@link ChatCommand} annotation.
     * @param annotation Chat command annotation.
     * @param method Method which will invoke the command.
     * @return Built Command object.
     */
    public static Command fromAnnotation(final ChatCommand annotation, final Method method) {
        return Command.builder()
            .name(annotation.value())
            .aliases(annotation.aliases())
            .method(method)
            .description(annotation.description())
            .level(annotation.level())
            .build();
    }

    /**
     * Checks if a command has duplicate command name or aliases.
     * @param other Other command to compare to this one.
     * @return <code>true</code> if the other command has a command name or alias which
     *         matches this command's name or one of its aliases;
     *         <code>false</code> otherwise.
     */
    public boolean isDuplicate(final Command other) {
        final Set<String> thisAliases = new HashSet<>(this.aliases);
        thisAliases.add(this.getName());

        final Set<String> otherAliases = new HashSet<>(other.getAliases());
        otherAliases.add(other.getName());

        thisAliases.retainAll(otherAliases);
        return !thisAliases.isEmpty();
    }
}
