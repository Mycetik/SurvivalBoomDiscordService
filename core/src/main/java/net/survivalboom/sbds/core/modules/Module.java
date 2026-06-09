package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigManager;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigTemplate;
import net.survivalboom.sbds.api.libraries.ILibrary;
import net.survivalboom.sbds.api.modules.*;
import net.survivalboom.sbds.api.utils.valid.Valid;
import net.survivalboom.sbds.core.libraries.DynamicClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Module extends Valid implements IModule {

    private final ModuleManager manager;

    private final @Nullable ModuleFile file;

    private final File dataDir;


    private ConfigurationNode config;

    private IGuildConfigTemplate guildConfig;


    private final Logger logger;


    private final ModuleMeta meta;

    private final DynamicClassLoader classLoader;

    private final List<ILibrary> libraries = new ArrayList<>();

    protected ModuleMain moduleMain;


    protected boolean enabled = false;


    public Module(
            @NotNull ModuleMeta meta,
            @Nullable ModuleFile file,
            @NotNull File dataDir,
            @Nullable Collection<ILibrary> libraries,
            @NotNull ModuleManager manager
    ) {

        this.meta = meta;
        this.file = file;
        this.dataDir = dataDir;

        this.logger = LoggerFactory.getLogger(meta.getName());

        this.classLoader = new DynamicClassLoader("Module-" + meta.getName(), Module.class.getClassLoader());

        if (libraries != null) {
            this.libraries.addAll(libraries);
        }

        this.manager = manager;

    }


    //
    // GETTERS
    //

    // MODULE INFO //

    @Override
    public @NotNull String getName() {
        return meta.getName();
    }

    @Override
    public @NotNull ModuleMeta getMeta() {
        return meta;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    // MODULE LOCATION //

    @Override
    public @Nullable ModuleFile getFile() {
        return file;
    }

    @Override
    public @NotNull File getDataFolder() {
        return dataDir;
    }

    // GUILD CONFIG //

    @Override
    public IGuildConfigTemplate getGuildConfig() {
        return guildConfig;
    }

    @Override
    public @NotNull IGuildConfigTemplate createGuildConfig(@NotNull Consumer<IGuildConfigManager.IGuildConfigBuilder> builder) {

        if (guildConfig != null) {
            throw new IllegalStateException("Guild config already exists");
        }

        guildConfig = manager.getSbds().getGuildConfigManager().registerTemplate(this, builder);

        return guildConfig;

    }

    // MODULE CONFIG //

    @Override
    public void saveConfig(@NotNull File file) {

        Objects.requireNonNull(file, "file == null");
        checkValid();

        try {
            YamlConfigurationLoader.builder().path(file.toPath()).buildAndSaveString(config);
        }

        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public @NotNull ConfigurationNode loadConfig(@NotNull File file) {

        Objects.requireNonNull(file, "file == null");
        checkValid();

        try {
            config = YamlConfigurationLoader.builder().path(file.toPath()).build().load();
        }

        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return config;

    }

    @Override
    public @NotNull ConfigurationNode checkAndLoadConfig(@NotNull String fileName) {
        checkValid();
        moduleMain.checkFiles2(fileName);
        return loadConfig();
    }

    @Override
    public ConfigurationNode getConfig() {
        checkValid();
        return config;
    }


    // MODULE CLASS //

    @Override
    public @NotNull ModuleMain getMain() {
        return moduleMain;
    }

    @Override
    public @NotNull List<ILibrary> getLibraries() {
        return new ArrayList<>(libraries);
    }

    public @NotNull DynamicClassLoader getClassLoader() {
        return classLoader;
    }

    //
    // MISC
    //

    @Override
    public @NotNull IModuleManager getManager() {
        return manager;
    }

    @Override
    public String toString() {
        return meta.getName() + "@" + meta.getVersion();
    }

}
