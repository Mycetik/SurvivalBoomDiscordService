package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.ICommandManager;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.ContextCommandBase;
import net.survivalboom.sbds.api.commands.context.IContextCommandManager;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.database.IRepository;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.interaction.InteractionManager;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.interaction.dropdown.entity.EntityDropdownInteractionInfo;
import net.survivalboom.sbds.api.interaction.dropdown.string.StringDropdownInteractionInfo;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import net.survivalboom.sbds.api.interaction.modal.ModalTemplate;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public abstract class ModuleMain {

    private IModule module = null;

    //
    // LIFECYCLE (will be overridden in module's class)
    //

    public void onLoad() throws Throwable {}

    public void onUnload() throws Throwable {}


    public void onEnable() throws Throwable {}

    public void onDisable() throws Throwable {}

    //
    // INIT
    //

    public final void init(@NotNull IModule module) {
        if (this.module != null) throw new RuntimeException("Сука, ну написано же для таких долбоебов как ты 'Внутреннее API'. Ты слишком тупой чтобы использовать его, понимаешь? Не для тебя оно было сделано.");
        this.module = module;
    }


    public final @NotNull IModule getModule() {
        return module;
    }

    //
    // GETTERS
    //

    public @NotNull String getName() {
        return getModule().getName();
    }

    public @NotNull File getFile() {
        return getModule().getFile();
    }

    public @NotNull File getDataFolder() {
        return getModule().getDataFolder();
    }

    public @NotNull JarFile getJar() {
        return getModule().getJar();
    }

    public @NotNull IModuleManager getModuleManager() {
        return getModule().getModuleManager();
    }

    public @NotNull IDatabase getDatabase() {
        return getSbds().getDatabase();
    }

    public @NotNull YamlConfiguration getConfig() {
        return getModule().getConfig();
    }

    public @NotNull Logger getLogger() {
        return getModule().getLogger();
    }

    public @NotNull IModuleMeta getMeta() {
        return getModule().getMeta();
    }

    public @NotNull ISBDS getSbds() {
        return getModule().getSbds();
    }

    //
    // REGISTRATIONS
    //

    public @NotNull ICommandManager.RegisteredCommand registerSlashCommand(@NotNull CommandBase commandBase) {
        return getSbds().getSlashCommandManager().registerCommand(this, commandBase);
    }

    public @NotNull ICommandManager.RegisteredCommand registerConsoleCommand(@NotNull CommandBase commandBase) {
        return getSbds().getConsoleListener().registerCommand(this, commandBase);
    }

    public @NotNull IModalInteractionManager.IRegisteredModal registerModal(@NotNull String name, @NotNull ModalTemplate template) {
        return getSbds().getModalInteractionManager().registerModal(this, name, template);
    }

    public @NotNull InteractionManager.IRegisteredListener registerButton(@NotNull String name, @NotNull Consumer<ButtonInteractionInfo> consumer) {
        return getSbds().getButtonInteractionManager().registerListener(this, name, consumer);
    }

    public @NotNull InteractionManager.IRegisteredListener registerStringDropdown(@NotNull String name, @NotNull Consumer<StringDropdownInteractionInfo> consumer) {
        return getSbds().getStringDropdownInteractionManager().registerListener(this, name, consumer);
    }

    public @NotNull InteractionManager.IRegisteredListener registerEntityDropdown(@NotNull String name, @NotNull Consumer<EntityDropdownInteractionInfo> consumer) {
        return getSbds().getEntityDropdownInteractionManager().registerListener(this, name, consumer);
    }

    public @NotNull IContextCommandManager.RegisteredContextCommand registeredContextCommand(@NotNull ContextCommandBase base) {
        return getSbds().getContextCommandManager().registerContextCommand(this, base);
    }

    public void addModuleTranslations() {
        getSbds().getTranslationManager().addModuleTranslations(this);
    }

    public @NotNull IRepository createRepository(@NotNull String name, @NotNull RepositoryHandler<?> handler) {
        return getDatabase().createRepository(getModule(), name, handler);
    }

    //
    // CONFIG
    //

    public @NotNull YamlConfiguration saveDefaultConfig() {
        return saveDefaultConfig("config.yml");
    }

    public @NotNull YamlConfiguration saveDefaultConfig(@NotNull String fileName) {

        Objects.requireNonNull(fileName, "filename == null");

        File configFile = new File(getModule().getDataFolder(), fileName);
        try {
            checkFiles(Map.of(fileName, fileName));
            getConfig().load(configFile);
        }

        catch (IOException | InvalidConfigurationException e) {
            getLogger().warn("Failed to load configuration file `{}`", fileName, e);
        }

        return getConfig();

    }

    public void checkFiles(@NotNull Map<String, String> map) {
        Objects.requireNonNull(map, "map == null");
        CommonUtils.checkFiles(this.getClass(), getModule().getDataFolder(), map, null);
    }

}
