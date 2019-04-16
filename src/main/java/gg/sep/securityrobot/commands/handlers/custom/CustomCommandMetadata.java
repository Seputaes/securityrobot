package gg.sep.securityrobot.commands.handlers.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Model for Custom Command Metadata.
 */
@Builder
public class CustomCommandMetadata {
    @Getter @Setter private String commandName;
    private String aliases;
    private String level;
    @Getter private String description;
    @Getter private String response;
    private String showInCommandList;
    private String cooldown;

    /**
     * Returns the double value of the command's level.
     * @return Double value of the commands level.
     */
    public double getLevel() {
        return Double.parseDouble(level);
    }

    /**
     * Returns the long value of the command's cooldown.
     * @return Long value of the command's cooldown.
     */
    public long getCooldown() {
        if (cooldown == null) {
            return 0;
        }
        return Long.parseLong(cooldown);
    }

    /**
     * Returns whether the command should be shown in the command list.
     * @return {@code true} if the command will be shown in the command list.
     */
    public boolean isShownInCommandList() {
        return Boolean.parseBoolean(showInCommandList);
    }

    /**
     * Returns a list of the aliases which can also trigger the command.
     * @return List of the aliases which can also trigger the command.
     */
    public List<String> getAliases() {
        if (aliases == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(aliases.split("\\|"));
    }
}
