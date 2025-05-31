package net.survivalboom.sbds.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.core.commands.context.ContextCommandManager;
import net.survivalboom.sbds.core.commands.slash.SlashCommandManager;
import net.survivalboom.sbds.core.commands.console.ConsoleListener;
import net.survivalboom.sbds.core.database.Database;
import net.survivalboom.sbds.core.events.EventManager;
import net.survivalboom.sbds.core.interaction.button.ButtonInteractionManager;
import net.survivalboom.sbds.core.interaction.command.CommandInteractionManager;
import net.survivalboom.sbds.core.interaction.dropdown.entity.EntityDropdownInteractionManager;
import net.survivalboom.sbds.core.interaction.dropdown.string.StringDropdownInteractionManager;
import net.survivalboom.sbds.core.interaction.modal.ModalInteractionManager;
import net.survivalboom.sbds.core.libraries.LibrariesManager;
import net.survivalboom.sbds.core.messages.Messages;
import net.survivalboom.sbds.core.modules.ModuleManager;
import net.survivalboom.sbds.core.monitor.SystemMonitor;
import net.survivalboom.sbds.core.permissions.PermissionManager;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import net.survivalboom.sbds.api.SbdsProvider;
import net.survivalboom.sbds.core.translations.TranslationManager;
import org.bspfsystems.yamlconfiguration.configuration.Configuration;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.EnumSet;

public class SBDS implements ISBDS {

    private final Logger logger;

    private final YamlConfiguration configuration;

    private final File workingDir;

    private final LibrariesManager librariesManager;


    private final Scheduler scheduler;

    private final SystemMonitor systemMonitor;


    private final Database database;

    private final ModuleManager moduleManager;

    private final EventManager eventManager;


    private final CommandInteractionManager commandInteractionManager;

    private final ConsoleListener consoleListener;

    private final SlashCommandManager slashCommandManager;

    private final ContextCommandManager contextCommandManager;

    private final PermissionManager permissionManager;


    private final ButtonInteractionManager buttonInteractionManager;

    private final StringDropdownInteractionManager stringDropdownInteractionManager;

    private final EntityDropdownInteractionManager entityDropdownInteractionManager;

    private final ModalInteractionManager modalInteractionManager;


    private final TranslationManager translationManager;

    private final Messages messages;


    private boolean started = false;


    private final JDABuilder jdaBuilder;

    private JDA bot = null;


    public SBDS(@NotNull Logger logger, @NotNull LibrariesManager librariesManager, @NotNull YamlConfiguration configuration, @NotNull File workingDir, @NotNull String token) {

        this.logger = logger;
        this.configuration = configuration;
        this.jdaBuilder = JDABuilder.createDefault(token, EnumSet.allOf(GatewayIntent.class));
        this.workingDir = workingDir;

        this.librariesManager = librariesManager;

        this.scheduler = new Scheduler(this);
        this.systemMonitor = new SystemMonitor(scheduler);

        this.database = new Database(this);

        this.eventManager = new EventManager(this);
        this.moduleManager = new ModuleManager(this);

        this.translationManager = new TranslationManager(this);
        this.messages = new Messages(this);

        this.consoleListener = new ConsoleListener(this);
        this.permissionManager = new PermissionManager(this);
        this.commandInteractionManager = new CommandInteractionManager(this);
        this.contextCommandManager = new ContextCommandManager(this);
        this.slashCommandManager = new SlashCommandManager(this);

        this.buttonInteractionManager = new ButtonInteractionManager(this);
        this.stringDropdownInteractionManager = new StringDropdownInteractionManager(this);
        this.entityDropdownInteractionManager = new EntityDropdownInteractionManager(this);
        this.modalInteractionManager = new ModalInteractionManager(this);

        SbdsProvider.internal_internal_internal_internal_internal_internal_set(this);

    }

    //
    // LIFECYCLE
    //

    public synchronized void launch() throws InterruptedException {

        if (started) throw new IllegalStateException("Already started");

        started = true;

        librariesManager.configure(this);

        scheduler.init();
        systemMonitor.init();

        database.init();

        logger.info("Logging in...");

        try {
            bot = jdaBuilder.build();
        }

        catch (InvalidTokenException e) {
            logger.warn("Bot token is invalid.");
            throw new RuntimeException(e);
        }

        bot.awaitReady();

        logger.info("Logged successfully! ({}#{})", bot.getSelfUser().getName(), bot.getSelfUser().getDiscriminator());

        bot.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.customStatus("Starting SBDS v" + BuildConstants.VERSION + "..."));

        translationManager.init();
        messages.init();

        permissionManager.init();
        eventManager.init();

        commandInteractionManager.init();
        slashCommandManager.init();
        contextCommandManager.init();
        consoleListener.init();

        buttonInteractionManager.init();
        stringDropdownInteractionManager.init();
        entityDropdownInteractionManager.init();
        modalInteractionManager.init();

        moduleManager.init();
        consoleListener.startListener();
        slashCommandManager.updateCommands();

        bot.getPresence().setPresence(OnlineStatus.IDLE, Activity.customStatus("Running on SBDS v" + BuildConstants.VERSION + "🦖"));

        logger.info("");
        logger.info("SurvivalBoom Discord Service successfully started!");
        logger.info("");

    }

    public synchronized void shutdown() {

        if (!started) return;


        try {
            shutdown0();
        }

        catch (Throwable t) {
            logger.error("Failed to shutdown SBDS properly! This may cause data loss.", t);
            logger.error("Using System.exit() to shut down...");
            Main.exit();
        }

        started = false;

    }

    private void shutdown0() {

        logger.info("");
        logger.info("Stopping SurvivalBoom Discord Service...");

        moduleManager.shutdown();

        consoleListener.shutdown();
        slashCommandManager.shutdown();
        permissionManager.shutdown();
        contextCommandManager.shutdown();
        commandInteractionManager.shutdown();

        buttonInteractionManager.shutdown();
        entityDropdownInteractionManager.shutdown();
        stringDropdownInteractionManager.shutdown();
        modalInteractionManager.shutdown();

        translationManager.shutdown();
        messages.shutdown();

        eventManager.shutdown();

        database.shutdown();

        systemMonitor.shutdown();

        scheduler.shutdown();

        logger.info("Stopping bot...");

        bot.shutdown();
        bot = null;

        logger.info("Bye bye!");

    }

    public void blockThread() {
        CommonUtils.waitUntil(() -> !started);
    }

    //
    // GETTERS
    //

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public @NotNull JDA getBot() {
        return bot;
    }

    @Override
    public @NotNull File getWorkingDir() {
        return workingDir;
    }


    @Override
    public @NotNull ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public @NotNull EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public @NotNull PermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public @NotNull ConsoleListener getConsoleListener() {
        return consoleListener;
    }

    @Override
    public @NotNull SlashCommandManager getSlashCommandManager() {
        return slashCommandManager;
    }

    @Override
    public @NotNull ContextCommandManager getContextCommandManager() {
        return contextCommandManager;
    }


    @Override
    public @NotNull ModalInteractionManager getModalInteractionManager() {
        return modalInteractionManager;
    }

    @Override
    public @NotNull ButtonInteractionManager getButtonInteractionManager() {
        return buttonInteractionManager;
    }

    @Override
    public @NotNull StringDropdownInteractionManager getStringDropdownInteractionManager() {
        return stringDropdownInteractionManager;
    }

    @Override
    public @NotNull EntityDropdownInteractionManager getEntityDropdownInteractionManager() {
        return entityDropdownInteractionManager;
    }

    @Override
    public @NotNull Database getDatabase() {
        return database;
    }

    @Override
    public @NotNull TranslationManager getTranslationManager() {
        return translationManager;
    }

    @Override
    public @NotNull Messages getMessages() {
        return messages;
    }

    @Override
    public @NotNull String getVersion() {
        return BuildConstants.VERSION;
    }

    @Override
    public @NotNull Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public @NotNull SystemMonitor getSystemMonitor() {
        return systemMonitor;
    }

    @Override
    public @NotNull LibrariesManager getLibrariesManager() {
        return librariesManager;
    }


    public @NotNull CommandInteractionManager getCommandInteractionManager() {
        return commandInteractionManager;
    }

    //
    // STATIC
    //

    public static @NotNull SBDS getInstance() {
        return sbds;
    }

    protected static SBDS sbds = null;

}
