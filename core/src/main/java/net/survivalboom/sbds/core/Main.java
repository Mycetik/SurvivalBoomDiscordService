package net.survivalboom.sbds.core;

import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.core.logging.LoggerLayout;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class Main {

    private static boolean started = false;

    public static void main(String[] args) {

        if (started) throw new RuntimeException("Ну ты долбоеб, да? Я склоняюсь к мысли что да");
        started = true;

        File workingDir = CommonUtils.getJarFile(Main.class).getParentFile();

        LoggerLayout.setup();
        Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

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

        Main main = new Main(logger, workingDir);

        try {
            main.launch();
        }

        catch (Throwable t) {
            logger.error("Fatal error occurred. Exiting in 10 seconds...", t);
            exit();
        }

    }


    private final Logger logger;

    private final File workingDir;

    private final YamlConfiguration yamlConfiguration = new YamlConfiguration();


    private String token;


    public Main(@NotNull Logger logger, @NotNull File workingDir) {
        this.logger = logger;
        this.workingDir = workingDir;
    }


    private void launch() throws InterruptedException {


        checkFiles();
        loadConfiguration();

        sbdsStart().blockThread();

    }

    private boolean checkFiles() {

        logger.info("Checking files...");

        try {
            CommonUtils.checkFiles(Main.class, workingDir, Map.of("settings.yml", "settings.yml"), null);
        }

        catch (Throwable t) {
            logger.error("Failed to create required files", t);
            exit();
            return true;
        }

        return false;

    }

    private boolean loadConfiguration() {

        logger.info("Loading configuration...");

        try {
            yamlConfiguration.load(new File(workingDir, "settings.yml"));
            token = loadToken(new File(workingDir, "token"));
        }

        catch (Throwable t) {
            logger.error("Failed to load configuration. Exiting...", t);
            exit();
            return true;
        }

        if (token == null) {
            logger.warn("Token file is empty. Please provide a discord bot token. Exiting in 10 seconds...");
            exit();
            return true;
        }

        return false;

    }

    private @NotNull SBDS sbdsStart() throws InterruptedException {

        logger.info("Starting SBDS...");

        SBDS sbds = new SBDS(logger, yamlConfiguration, workingDir, token);
        SBDS.sbds = sbds;

        try {
            sbds.launch();
        }

        catch (InvalidTokenException e) {
            logger.warn("Bot token is invalid. Exiting in 10 seconds...");
            exit();
        }

        return sbds;

    }


    //
    // EXIT
    //

    private static void exit() {
        CommonUtils.sleep(10000);
        System.exit(1);
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
