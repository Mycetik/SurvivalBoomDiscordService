package net.survivalboom.sbds.api.libraries;

import net.survivalboom.sbds.api.modules.IModule;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public interface ILibrariesManager {

    String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";


    default boolean satisfy(@NotNull IModule module, @NotNull File file) throws IOException, InvalidConfigurationException {

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);

        return satisfy(module, yamlConfiguration);

    }

    boolean satisfy(@NotNull IModule module, @NotNull ConfigurationSection section);

}
