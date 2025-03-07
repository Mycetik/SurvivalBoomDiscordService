package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.modules.IModuleMeta;
import net.survivalboom.sbds.api.modules.InvalidModuleMetaException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.bspfsystems.yamlconfiguration.configuration.Configuration;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

// TODO Можливо додати прив'язку бібліотек до конкретних модулів і зберігати цю інформацію тут. Чому б ні?
public class ModuleMeta implements IModuleMeta {

    @NotNull private final String name;

    @NotNull private final String main;


    @Nullable private final String description;

    @NotNull private final String version;


    @Nullable private final String website;

    @NotNull private final List<String> authors;


    @NotNull private final Map<String, Dependency> dependencies = new HashMap<>();

    @Nullable private final ConfigurationSection librariesSection;


    public ModuleMeta(@NotNull Configuration configuration) throws InvalidModuleMetaException {

        Objects.requireNonNull(configuration, "configuration == null");

        String name = configuration.getString("name");
        String main = configuration.getString("main");

        if (name == null) throw new InvalidModuleMetaException("Key 'name' not found");
        if (main == null) throw new InvalidModuleMetaException("key 'main' not found");

        this.name = name;
        this.main = main;

        this.description = configuration.getString("description");
        this.version = configuration.getString("version", "0.0");

        this.website = configuration.getString("website");

        String author = configuration.getString("author");
        this.authors = author != null ? List.of(author) : configuration.getStringList("authors");

        this.librariesSection = configuration.getConfigurationSection("libraries");

        ConfigurationSection dependenciesSection = configuration.getConfigurationSection("dependencies");
        if (dependenciesSection == null) return;

        for (String s : dependenciesSection.getKeys(false)) {

            ConfigurationSection section = dependenciesSection.getConfigurationSection(s);
            if (section == null) continue;

            Dependency dependency = new Dependency(s, section);

            dependencies.put(s, dependency);

        }

    }


    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getMain() {
        return main;
    }


    public @Nullable String getDescription() {
        return description;
    }

    public @NotNull String getVersion() {
        return version;
    }


    public @Nullable String getWebsite() {
        return website;
    }

    public @NotNull List<String> getAuthors() {
        return new ArrayList<>(authors);
    }


    public @NotNull List<Dependency> getDependencies() {
        return new ArrayList<>(dependencies.values());
    }

    public @Nullable Dependency getDependency(@NotNull String name) {
        return dependencies.get(name);
    }

    public @Nullable ConfigurationSection getLibrariesSection() {
        return librariesSection;
    }


    //
    // STATIC
    //

    public static @NotNull ModuleMeta loadFrom(@NotNull InputStream stream) throws InvalidModuleMetaException, IOException {

        YamlConfiguration yamlConfiguration = new YamlConfiguration();

        try {
            yamlConfiguration.load(new InputStreamReader(stream));
        }

        catch (InvalidConfigurationException e) {
            throw new InvalidModuleMetaException(e);
        }

        return new ModuleMeta(yamlConfiguration);

    }


    public static class Dependency implements IDependency {

        private final String name;

        private final LoadOrder order;

        private final boolean required;

        private final boolean joinClasspath;

        public Dependency(@NotNull String name, @NotNull ConfigurationSection section) {

            Objects.requireNonNull(name, "name == null");
            Objects.requireNonNull(section, "section == null");

            this.name = name;

            this.required = section.getBoolean("required");
            this.joinClasspath = section.getBoolean("join-classpath");

            this.order = Objects.requireNonNullElse(CommonUtils.getEnumValue(LoadOrder.class, section.getString("load")), LoadOrder.BEFORE);

        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public @NotNull LoadOrder getOrder() {
            return order;
        }

        @Override
        public boolean required() {
            return required;
        }

        @Override
        public boolean joinClasspath() {
            return joinClasspath;
        }



    }

}
