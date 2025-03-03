package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.ILibrary;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Library implements ILibrary {

    private final String url;


    private final String name;

    private final String description;


    private final LibrarySearchInfo info;

    private final ConfigurationSection pom;

    private final List<Library> dependencies;


    public Library(@NotNull LibrarySearchInfo info, @Nullable String url, @NotNull List<Library> dependencies, @NotNull ConfigurationSection pom) {

        Objects.requireNonNull(info, "info == null");
        Objects.requireNonNull(dependencies, "dependencies == null");
        Objects.requireNonNull(pom, "pom == null");

        this.info = info;
        this.url = url;
        this.pom = pom;
        this.dependencies = dependencies;

        this.name = pom.getString("project.name");
        this.description = pom.getString("project.description");

    }


    public @NotNull ConfigurationSection getPom() {
        return pom;
    }


    public @Nullable String getUrl() {
        return url;
    }

    public @NotNull LibrarySearchInfo getInfo() {
        return info;
    }


    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public @NotNull List<Library> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    @Override
    public String toString() {
        return String.format("Library{name=%s, gradle=%s, dependencies=%s}", name, info.gradle(), String.join(", ", dependencies.toString()));
    }

}
