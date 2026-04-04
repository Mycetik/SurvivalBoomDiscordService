package net.survivalboom.sbds.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.registrations.IRegistrationRegistry;
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
import net.survivalboom.sbds.core.registration.RegistrationRegistry;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import net.survivalboom.sbds.api.SbdsProvider;
import net.survivalboom.sbds.core.service.ServiceProvider;
import net.survivalboom.sbds.core.translations.TranslationManager;
import org.bspfsystems.yamlconfiguration.configuration.Configuration;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class SBDS implements ISBDS {

    private final Logger logger;

    private final YamlConfiguration configuration;

    private final File workingDir;

    private final LibrariesManager librariesManager;


    private final Scheduler scheduler;

    private final SystemMonitor systemMonitor;


    private final Database database;

    private final ModuleManager moduleManager;

    private final RegistrationRegistry registrationRegistry;

    private final ServiceProvider serviceProvider;

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

    private boolean ready = false;


    private final JDABuilder jdaBuilder;
    private static final EnumSet<GatewayIntent> DEFAULT_GATEWAY_INTENTS = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_INVITES,
            GatewayIntent.GUILD_WEBHOOKS,
            GatewayIntent.GUILD_MESSAGE_TYPING,
            GatewayIntent.GUILD_MODERATION,
            GatewayIntent.AUTO_MODERATION_CONFIGURATION,
            GatewayIntent.AUTO_MODERATION_EXECUTION
    );
    private static final EnumSet<GatewayIntent> PRIVILEGED_GATEWAY_INTENTS = EnumSet.of(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.MESSAGE_CONTENT
    );

    private JDA bot = null;


    public SBDS(@NotNull Logger logger, @NotNull LibrariesManager librariesManager, @NotNull YamlConfiguration configuration, @NotNull File workingDir, @NotNull String token) {

        this.logger = logger;
        this.configuration = configuration;
        this.jdaBuilder = JDABuilder.createDefault(token, resolveGatewayIntents(configuration));
        this.workingDir = workingDir;

        this.librariesManager = librariesManager;

        this.scheduler = new Scheduler(this);
        this.systemMonitor = new SystemMonitor(scheduler);

        this.database = new Database(this);

        this.eventManager = new EventManager(this);
        this.moduleManager = new ModuleManager(this);
        this.registrationRegistry = new RegistrationRegistry(this);
        this.serviceProvider = new ServiceProvider(this);

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

        serviceProvider.init();
        moduleManager.init();
        registrationRegistry.init();
        slashCommandManager.updateCommands();

        bot.getPresence().setPresence(OnlineStatus.IDLE, Activity.customStatus("Running on SBDS v" + BuildConstants.VERSION + "🦖"));

        logger.info("");
        logger.info("SurvivalBoom Discord Service successfully started!");
        logger.info("");

        ready = true;

    }

    public synchronized void shutdown() {

        if (!started) return;

        ready = false;

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

        bot.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.customStatus("Shutting down SBDS v" + BuildConstants.VERSION + "..."));

        registrationRegistry.shutdown();
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
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean isStarted() {
        return started;
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
    public @NotNull IRegistrationRegistry getRegistrationRegistry() {
        return registrationRegistry;
    }

    @Override
    public @NotNull ServiceProvider getServiceProvider() {
        return serviceProvider;
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

    private @NotNull EnumSet<GatewayIntent> resolveGatewayIntents(@NotNull YamlConfiguration configuration) {

        EnumSet<GatewayIntent> intents = EnumSet.copyOf(DEFAULT_GATEWAY_INTENTS);

        ConfigurationSection discordSection = configuration.getConfigurationSection("discord");
        if (discordSection == null) {
            logger.info("No discord.gateway-intents section found. Using safe defaults: {}", intents);
            return intents;
        }

        List<String> configured = discordSection.getStringList("gateway-intents");
        if (configured == null || configured.isEmpty()) {
            logger.info("discord.gateway-intents not configured. Using safe defaults: {}", intents);
            return intents;
        }

        EnumSet<GatewayIntent> parsed = EnumSet.noneOf(GatewayIntent.class);
        for (String name : configured) {
            if (name == null || name.isBlank()) continue;

            String trimmed = name.trim();
            try {
                GatewayIntent intent = GatewayIntent.valueOf(trimmed.toUpperCase(Locale.ROOT));
                parsed.add(intent);
            }
            catch (IllegalArgumentException ex) {
                logger.warn("Unknown gateway intent '{}' in settings.yml. Skipping...", trimmed);
            }
        }

        if (parsed.isEmpty()) {
            logger.warn("No valid gateway intents configured; falling back to safe defaults: {}", intents);
            return intents;
        }

        EnumSet<GatewayIntent> privileged = EnumSet.copyOf(PRIVILEGED_GATEWAY_INTENTS);
        privileged.retainAll(parsed);
        if (!privileged.isEmpty()) {
            logger.warn("Privileged gateway intents requested: {}. Ensure they are enabled in the Discord developer portal.", privileged);
        }

        logger.info("Using configured gateway intents: {}", parsed);
        return parsed;
    }

    //
    // STATIC
    //

    public static @NotNull SBDS getInstance() {
        return sbds;
    }

    protected static SBDS sbds = null;

}
