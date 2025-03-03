package net.survivalboom.sbds.core;

import net.survivalboom.sbds.core.libraries.LibrarySectionParseException;
import net.survivalboom.sbds.core.libraries.UnknownDependencyException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.core.libraries.JarLoader;
import net.survivalboom.sbds.core.libraries.LibrariesManager;
import net.survivalboom.sbds.core.logging.LoggerLayout;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class SbdsBootstrap {

    private final Logger logger;

    private final File workingDir;

    private final LibrariesManager librariesManager;

    private final YamlConfiguration yamlConfiguration = new YamlConfiguration();

    private String token;


    public SbdsBootstrap(@NotNull File workingDir, @NotNull JarLoader jarLoader) {
        this.logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        this.workingDir = workingDir;
        this.librariesManager = new LibrariesManager(new File(workingDir, "libraries"), jarLoader);
    }


    public void launch() {

        LoggerLayout.setup();

        logger.info("");
        logger.info("    ____              _           _____                ");
        logger.info("   / __/_ _______  __(_)  _____ _/ / _ )___  ___  __  _ ");
        logger.info("  _\\ \\/ // / __/ |/ / / |/ / _ `/ / _  / _ \\/ _ \\/  '  /");
        logger.info(" /___/\\_,_/_/  |___/_/|___/\\_,_/_/____/\\___/\\___/_/_/_/");
        logger.info("");
        logger.info("SurvivalBoom Network 2025 | SurvivalBoom Discord Service");
        logger.info("                    By TIMURishche \uD83E\uDD96");
        logger.info("");
        logger.info("                    Version {}", BuildConstants.VERSION);
        logger.info("");

        try {

            checkFiles();
            loadConfiguration();
            checkLibraries();

            sbdsStart().blockThread();

        }

        catch (Throwable t) {
            logger.error("Fatal error occurred. Exiting in 10 seconds...", t);
            Main.exit();
        }


    }

    private void checkFiles() {

        logger.info("Checking files...");

        try {
            CommonUtils.checkFiles(Main.class, workingDir, Map.of("settings.yml", "settings.yml"), null);
        }

        catch (Throwable t) {
            logger.error("Failed to create required files", t);
            Main.exit();
        }

    }

    private void checkLibraries() {

        logger.info("Loading libraries...");

        ConfigurationSection section = yamlConfiguration.getConfigurationSection("libraries");
        if (section == null) {
            logger.warn("Libraries section does not exist or is empty. SBDS may crash!");
            return;
        }

        boolean success = librariesManager.satisfy0(null, section, true);
        if (!success) {
            logger.error("Some libraries were failed to download. Refusing to start.");
            throw new RuntimeException();
        }

    }

    private boolean loadConfiguration() {

        logger.info("Loading configuration...");

        try {
            yamlConfiguration.load(new File(workingDir, "settings.yml"));
            token = loadToken(new File(workingDir, "token"));
        }

        catch (Throwable t) {
            logger.error("Failed to load configuration. Exiting...", t);
            Main.exit();
            return true;
        }

        if (token == null) {
            logger.warn("Token file is empty. Please provide a discord bot token. Exiting in 10 seconds...");
            Main.exit();
            return true;
        }

        return false;

    }

    private @NotNull SBDS sbdsStart() throws InterruptedException {

        logger.info("Starting SBDS...");

        SBDS sbds = new SBDS(logger, librariesManager, yamlConfiguration, workingDir, token);
        SBDS.sbds = sbds;

        sbds.launch();

        return sbds;

    }

    //
    // UTILS
    //

    private static @Nullable String loadToken(@NotNull File file) throws IOException {

        if (!file.exists()) file.createNewFile();

        List<String> lines = Files.readAllLines(file.toPath());
        if (lines.isEmpty()) return null;

        return lines.getFirst();

    }

}
