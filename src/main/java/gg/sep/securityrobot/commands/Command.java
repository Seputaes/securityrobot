package gg.sep.securityrobot.commands;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.commands.handlers.custom.CustomCommandMetadata;

/**
 * Model of a bot command with builder.
 */
@Builder
@Getter
@Log4j2
public class Command {

    private String name;
    private String description;
    private Set<String> aliases;
    private boolean isCustom;
    private CustomCommandMetadata customMetadata;
    private Method method;
    private CommandLevel level;
    private boolean shownInCommandList;
    private int cooldown;

    private Stopwatch lastExecuted; // TODO: There has to be a better way to do this.

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
     * Builds all of the command tree options which can trigger a command, and handles sub-commands.
     *
     * For example, if a command string is: "points add" and has aliases of "gold, rewards", this
     * method will produce a set containing these ordered lists:
     *
     *    1. [points, add]
     *    2. [gold, add]
     *    3. [rewards, add]
     *
     * This effectively generates a completely unique signature for the command.
     * @return Set of all possible command branches, including aliases.
     */
    public Set<List<String>> getCommandTree() {
        final Set<List<String>> commandTree = new HashSet<>();
        final String[] splitMainCommand = getName().split(" ");

        for (final String trigger : getTriggerStrings()) {
            final List<String> triggerList = new LinkedList<>();
            triggerList.add(trigger);
            if (splitMainCommand.length > 1) {
                triggerList.addAll(Arrays.asList(Arrays.copyOfRange(splitMainCommand, 1, splitMainCommand.length)));
            }
            commandTree.add(triggerList);
        }
        return commandTree;
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
        return o.getCommandTree().equals(this.getCommandTree());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.getCommandTree());
    }

    /**
     * Gets the help text for this command.
     * @return The help text for this command.
     */
    public String getHelp() {
        return this.description;
    }

    /**
     * Checks whether the time between the last command execution and now has elapsed.
     * @return Whether the cooldown has expired.
     */
    public boolean cooldownElapsed() {
        if (!lastExecuted.isRunning()) {
            lastExecuted.start();
            return true;
        }
        return lastExecuted.elapsed().toSeconds() >= cooldown;
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
     * Returns all possible FIRST word trigger strings. This should ONLY be used in the context of
     * custom commands.
     * @return All possible trigger strings for a command.
     */
    public Set<String> getTriggerStrings() {
        if (!this.isCustom()) {
            log.fatal("getTriggerStrings called on a non-custom command. " +
                "This should never happen and is NOT safe. Command: {}", this.getName());
        }
        final Set<String> thisAliases = new HashSet<>(this.aliases);
        final String mainCommand = getName().split(" ")[0];
        thisAliases.add(mainCommand);
        return thisAliases;
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
            .shownInCommandList(annotation.showInCommandList())
            .method(method)
            .isCustom(false)
            .customMetadata(null)
            .description(annotation.description())
            .level(annotation.level())
            .cooldown(annotation.cooldown())
            .lastExecuted(Stopwatch.createUnstarted())
            .build();
    }

    /**
     * Build a custom command from its metadata.
     * @param metadata Metadata of the custom command.
     * @return Built command.
     */
    public static Command fromCommandMetadata(final CustomCommandMetadata metadata) {
        return Command.builder()
            .name(metadata.getCommandName())
            .aliases(metadata.getAliases())
            .shownInCommandList(metadata.isShownInCommandList())
            .method(null)
            .isCustom(true)
            .customMetadata(metadata)
            .description(metadata.getDescription())
            .level(CommandLevel.parseRequiredLevelString(String.valueOf(metadata.getLevel())))
            .cooldown((int) metadata.getCooldown())
            .lastExecuted(Stopwatch.createUnstarted())
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
