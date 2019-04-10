package gg.sep.securityrobot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.config.models.ApplicationConfig;
import gg.sep.securityrobot.utils.ModelParser;

/**
 * Utility class for loading up the initial application configuration files.
 */
@Log4j2
@UtilityClass
public class ConfigLoader {

    private static final String CONFIG_FILE_NAME = "configuration/app.json";
    private static final String CONFIG_OVERRIDE_FILE_NAME = "configuration/app_override.json";

    /**
     * Loads the Application config files off of the disk and returns a new Application Config instance.
     * @return Loaded Application Config instance parsed from the files on the disk.
     */
    public static ApplicationConfig loadConfig() {
        return ModelParser.parseJson(getConfigFileContents(), ApplicationConfig.class)
            .orElseThrow(() -> new RuntimeException("Unable to parse App config files."));
    }

    /**
     * Attempt to load the override configuration file from the disk.
     * @return Raw config json contents.
     */
    private static String getConfigFileContents() {
        try {
            final InputStream configInputStream;

            final File overrideFile = new File(CONFIG_OVERRIDE_FILE_NAME);
            final File mainConfigFile = new File(CONFIG_FILE_NAME);

            if (overrideFile.exists()) {
                configInputStream = new FileInputStream(overrideFile);
            } else {
                configInputStream = new FileInputStream(mainConfigFile);
            }
            return CharStreams.toString(new InputStreamReader(configInputStream, Charsets.UTF_8));
        } catch (final IOException e) {
            log.error("Error loading the configuration files.");
            throw new RuntimeException(e);
        }
    }
}
