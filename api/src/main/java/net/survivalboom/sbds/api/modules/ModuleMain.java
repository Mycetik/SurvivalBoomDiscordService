package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.IConsoleListener;
import net.survivalboom.sbds.api.commands.context.IContextCommandManager;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class ModuleMain {

    private ISBDS sbds;

    private IModule module;

    //
    // LIFECYCLE (will be overridden in module's class)
    //

    @ApiStatus.OverrideOnly
    public void onLoad() throws Exception {}

    @ApiStatus.OverrideOnly
    public void onUnload() throws Exception {}


    @ApiStatus.OverrideOnly
    public void onEnable() throws Exception {}

    @ApiStatus.OverrideOnly
    public void onDisable() throws Exception {}

    //
    // INIT
    //

    @ApiStatus.Internal
    public final void init(@NotNull IModule module, @NotNull ISBDS sbds) {

        if (this.module != null) {
            throw new RuntimeException("Сука, ну написано же для таких долбоебов как ты 'Внутреннее API'. Ты слишком тупой чтобы использовать его, понимаешь? Не для тебя оно было сделано.");
        }

        this.module = module;
        this.sbds = sbds;

    }


    public final @NotNull IModule getModule() {
        return module;
    }

    public final @NotNull ISBDS getSbds() {
        return sbds;
    }

    //
    // GETTERS
    //

    public @NotNull String getName() {
        return getModule().getName();
    }

    public @Nullable ModuleFile getFile() {
        return getModule().getFile();
    }

    public @NotNull File getDataFolder() {
        return getModule().getDataFolder();
    }

    public @NotNull IModuleManager getModuleManager() {
        return getSbds().getModuleManager();
    }

    public @NotNull IDatabase getDatabase() {
        return getSbds().getDatabase();
    }

    public @NotNull Logger getLogger() {
        return getModule().getLogger();
    }

    public @NotNull ModuleMeta getMeta() {
        return getModule().getMeta();
    }

    //
    // REGISTRATIONS
    //

    // CONSOLE COMMANDS //

    public @NotNull IConsoleListener.IRegisteredConsoleCommand registerConsoleCommand(@NotNull Command command) {
        return sbds.getConsoleListener().registerCommand(this, command);
    }

    public @NotNull IConsoleListener.IRegisteredConsoleCommand registerConsoleCommand(@NotNull CommandBase command) {
        return sbds.getConsoleListener().registerCommand(this, command);
    }

    // SLASH COMMANDS //

    public @NotNull ISlashCommandManager.IRegisteredSlashCommand registerSlashCommand(@NotNull Command command) {
        return sbds.getSlashCommandManager().registerCommand(this, command);
    }

    public @NotNull ISlashCommandManager.IRegisteredSlashCommand registerSlashCommand(@NotNull CommandBase command) {
        return sbds.getSlashCommandManager().registerCommand(this, command);
    }

    // CONTEXT COMMANDS //

    public @NotNull IContextCommandManager.IRegisteredContextCommand registerContextCommand(@NotNull Command command) {
        return sbds.getContextCommandManager().registerCommand(this, command);
    }

    public @NotNull IContextCommandManager.IRegisteredContextCommand registerContextCommand(@NotNull CommandBase command) {
        return sbds.getContextCommandManager().registerCommand(this, command);
    }

    // TRANSLATIONS //

    public void addModuleTranslations() {
        addModuleTranslations("translations");
    }

    public void addModuleTranslations(@NotNull String... files) {

        Map<String, String> map = new HashMap<>();
        for (String file : files) {
            map.put("translations/" + file, "translations/" + file);
        }

        checkFiles(map);
        addModuleTranslations();

    }

    public void addModuleTranslations(@NotNull String directory) {
        sbds.getTranslationManager().importModuleMessages(this, directory);
    }


    //
    // CONFIG
    //

    public void checkFiles(@NotNull Map<String, String> map) {
        CommonUtils.checkFiles(this.getClass(), getModule().getDataFolder(), map, null);
    }

    public void checkFiles(@NotNull String... files) {
        CommonUtils.checkFiles(this.getClass(), getModule().getDataFolder(), null, files);
    }

    public void checkFiles2(@NotNull String... files) {

        Map<String, String> map = new HashMap<>();
        for (String file : files) {
            map.put(file, file);
        }

        checkFiles(map);

    }


    public @NotNull ConfigurationNode loadConfig(@NotNull File file) {
        return getModule().loadConfig(file);
    }

    public @NotNull ConfigurationNode loadConfig() {
        return getModule().loadConfig();
    }


    public void saveConfig(@NotNull File file) {
        getModule().saveConfig(file);
    }

    public void saveConfig() {
        getModule().saveConfig();
    }


    public @NotNull ConfigurationNode checkAndLoadConfig(@NotNull String fileName) {
        return getModule().checkAndLoadConfig(fileName);
    }

    public @NotNull ConfigurationNode checkAndLoadConfig() {
        return getModule().checkAndLoadConfig();
    }


    public @NotNull ConfigurationNode getConfig() {
        return getModule().getConfig();
    }

}
