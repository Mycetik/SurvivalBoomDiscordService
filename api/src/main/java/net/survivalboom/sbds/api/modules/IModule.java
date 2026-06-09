package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigManager;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigTemplate;
import net.survivalboom.sbds.api.libraries.ILibrary;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface IModule extends IValid {

    @NotNull IModuleManager getManager();

    // MODULE INFO //

    @NotNull String getName();

    @NotNull ModuleMeta getMeta();

    boolean isEnabled();

    @NotNull Logger getLogger();

    // MODULE LOCATION //

    @Nullable ModuleFile getFile();

    @NotNull File getDataFolder();

    // GUILD CONFIG //

    IGuildConfigTemplate getGuildConfig();

    @NotNull IGuildConfigTemplate createGuildConfig(@NotNull Consumer<IGuildConfigManager.IGuildConfigBuilder> builder);

    // CONFIG //

    void saveConfig(@NotNull File file);

    default void saveConfig() {
        saveConfig(new File(getDataFolder(), "config.yml"));
    }


    @NotNull ConfigurationNode loadConfig(@NotNull File file);

    default @NotNull ConfigurationNode loadConfig() {
        return loadConfig(new File(getDataFolder(), "config.yml"));
    }


    @NotNull ConfigurationNode checkAndLoadConfig(@NotNull String fileName);

    default @NotNull ConfigurationNode checkAndLoadConfig() {
        return checkAndLoadConfig("config.yml");
    }


    ConfigurationNode getConfig();

    // MODULE CLASS //

    @NotNull ModuleMain getMain();

    @NotNull List<ILibrary> getLibraries();

}
