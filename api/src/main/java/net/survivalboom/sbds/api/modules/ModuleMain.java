package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;

public abstract class ModuleMain {

    private IModule module = null;

    //
    // LIFECYCLE (will be overridden in module's class)
    //

    public void onLoad() {}

    public void onUnload() {}


    public void onEnable() {}

    public void onDisable() {}

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
    // GETTER
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

    public void registerSlashCommand(@NotNull CommandBase commandBase) {
        getSbds().getSlashCommandManager().registerCommand(this, commandBase);
    }

    public void addModuleTranslation() {
        getSbds().getTranslationManager().addModuleTranslations(this);
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
