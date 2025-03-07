package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.ILibrary;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Library implements ILibrary {


    private final String name;

    private final String description;

    private final String url; // URL з якого був завантажений POM цієї бібліотеки. Якщо pom був знайдений локально, url буде завантажено із XXX.pom.url.


    private final LibrarySearchInfo info;

    private final PomFile pom;

    private final List<String> repositories;

    private List<Library> dependencies;

    private Placeholders properties;

    private Library parent;


    private Map<String, String> bomDependenciesVersions;

    private List<Library> dependencyProviders;


    public Library(@NotNull LibrarySearchInfo info, @NotNull List<String> repositories, @NotNull PomFile pom) {

        Objects.requireNonNull(info, "info == null");
        Objects.requireNonNull(pom, "pom == null");
        Objects.requireNonNull(repositories, "repositories == null");

        ConfigurationSection pomCfg = pom.pom();

        this.url = pomCfg.getString("url");
        Objects.requireNonNull(url, "url was not found in pom section");

        this.info = info;
        this.pom = pom;
        this.repositories = new ArrayList<>(repositories);

        this.name = pomCfg.getString("project.name");
        this.description = pomCfg.getString("project.description");

    }

    //
    // GETTERS
    //

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public @NotNull LibrarySearchInfo getInfo() {
        return info;
    }

    public @NotNull PomFile getPom() {
        return pom;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public @NotNull List<String> getRepositories() {
        return new ArrayList<>(repositories);
    }


    public @Nullable String getBomVersion(@NotNull String rec) {

        Objects.requireNonNull(rec, "rec == null");
        if (bomDependenciesVersions.containsKey(rec)) return bomDependenciesVersions.get(rec);

        String version = dependencyProviders.stream().map(lib -> lib.getBomVersion(rec)).filter(Objects::nonNull).findAny().orElse(null);
        if (version != null) return version;

        if (parent == null) return null;

        return parent.getBomVersion(rec);

    }

    public @NotNull List<Library> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    public @Nullable Library getParent() {
        return parent;
    }

    public @NotNull Placeholders getProperties() {
        return properties;
    }


    //
    // SETTERS
    //

    public void setDependencies(@NotNull List<Library> list) {
        if (this.dependencies != null) throw new IllegalStateException("Already set");
        Objects.requireNonNull(list);
        this.dependencies = new ArrayList<>(list);
    }

    public void setParent(@NotNull Library parent) {
        if (this.parent != null) throw new IllegalStateException("Already set");
        Objects.requireNonNull(parent);
        this.parent = parent;
    }

    public void setProperties(@NotNull Placeholders placeholders) {
        if (this.properties != null) throw new IllegalStateException("Already set");
        Objects.requireNonNull(placeholders);
        this.properties = placeholders.copy();
    }


    public void setBomDependenciesVersions(@NotNull Map<String, String> map) {
        if (this.bomDependenciesVersions != null) throw new IllegalStateException("Already set");
        Objects.requireNonNull(map);
        this.bomDependenciesVersions = map;
    }

    public void setDependencyProviders(@NotNull List<Library> list) {
        if (this.dependencyProviders != null) throw new IllegalStateException("Already set");
        Objects.requireNonNull(list);
        this.dependencyProviders = list;
    }


    @Override
    public String toString() {
        return String.format("Library{name=%s, gradle=%s, dependencies=%s, parent=%s, dependenciesProviders=%s, dependenciesVersion=%s}", name, info.gradle(), dependencies, parent, dependencyProviders, bomDependenciesVersions);
    }

}
