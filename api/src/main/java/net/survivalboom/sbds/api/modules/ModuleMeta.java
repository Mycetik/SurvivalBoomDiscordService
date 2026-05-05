package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.modules.dependencies.LoadOrder;
import net.survivalboom.sbds.api.modules.dependencies.ModuleDependency;
import net.survivalboom.sbds.api.libraries.LibraryDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@ConfigSerializable
public final class ModuleMeta {

    private @Setting("name") String name;

    private @Setting("main") String main;

    private @Setting("api-version") @Nullable String apiVersion;


    private @Setting("description") @Nullable String description;

    private @Setting("version") String version;


    private @Setting("website") @Nullable String website;

    private @Setting("author") List<String> authors;


    private @Setting("dependencies") List<ModuleDependency> dependencies;

    private @Setting("libraries") List<LibraryDeclaration> libraries;


    public ModuleMeta(
            @NotNull String name,
            @NotNull String main,
            @Nullable String apiVersion,
            @Nullable String description,
            @NotNull String version,
            @Nullable String website,
            @Nullable Collection<String> authors,
            @Nullable Collection<ModuleDependency> dependencies,
            @Nullable Collection<LibraryDeclaration> libraries
    ) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(main, "main == null");
        Objects.requireNonNull(version, "version == null");

        this.name = name;
        this.main = main;
        this.apiVersion = apiVersion;

        this.description = description;
        this.version = version;

        this.website = website;
        this.authors = authors != null ? new ArrayList<>(authors) : new ArrayList<>();

        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
        this.libraries = libraries != null ? new ArrayList<>(libraries) : new ArrayList<>();

    }

    private ModuleMeta() {}

    @PostProcess
    private void validate() throws SerializationException {

        if (name == null) {
            throw new SerializationException("name == null");
        }

        if (main == null) {
            throw new SerializationException("main == null");
        }

        if (version == null) {
            throw new SerializationException("version == null");
        }


        if (authors == null) {
            authors = new ArrayList<>();
        }

        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }

        if (libraries == null) {
            libraries = new ArrayList<>();
        }

    }



    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getMain() {
        return main;
    }

    public @Nullable String getApiVersion() {
        return apiVersion;
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


    public @NotNull List<ModuleDependency> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    public @NotNull List<LibraryDeclaration> getLibraries() {
        return new ArrayList<>(libraries);
    }


    public @NotNull Builder copy() {
        return new Builder(this);
    }


    //
    // BUILDER
    //

    public static @NotNull ModuleMeta fromStream(@NotNull InputStream stream) throws InvalidModuleMetaException {

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .source(() -> new BufferedReader(new InputStreamReader(stream)))
                .build();

        ModuleMeta meta;
        try {
            meta = loader.load().get(ModuleMeta.class);
        }

        catch (ConfigurateException e) {
            throw new InvalidModuleMetaException(e);
        }

        if (meta == null) {
            throw new InvalidModuleMetaException("Failed to create meta from stream");
        }

        return meta;

    }

    public @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private String main;

        private @Nullable String apiVersion;


        private @Nullable String description;

        private String version;


        private @Nullable String website;

        private final List<String> authors = new ArrayList<>();


        private final List<ModuleDependency> dependencies = new ArrayList<>();

        private final List<LibraryDeclaration> libraries = new ArrayList<>();


        private Builder(Builder builder) {

            this.name = builder.name;
            this.main = builder.main;
            this.apiVersion = builder.apiVersion;

            this.description = builder.description;
            this.version = builder.version;

            this.website = builder.website;
            this.authors.addAll(builder.authors);

            this.dependencies.addAll(builder.dependencies);
            this.libraries.addAll(builder.libraries);

        }

        private Builder(ModuleMeta meta) {

            this.name = meta.name;
            this.main = meta.main;
            this.apiVersion = meta.apiVersion;

            this.description = meta.description;
            this.version = meta.version;

            this.website = meta.website;
            this.authors.addAll(meta.authors);

            this.dependencies.addAll(meta.dependencies);
            this.libraries.addAll(meta.libraries);

        }

        private Builder() {}

        // NAME //

        public @NotNull Builder setName(@NotNull String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        // MAIN //

        public @NotNull Builder setMain(@NotNull String main) {
            this.main = main;
            return this;
        }

        public @NotNull Builder setMain(@NotNull Class<? extends ModuleMain> clazz) {
            this.main = clazz.getName();
            return this;
        }

        public String getMain() {
            return main;
        }

        // API VERSION //

        public @NotNull Builder setApiVersion(@Nullable String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public @Nullable String getApiVersion() {
            return apiVersion;
        }

        // DESCRIPTION //

        public @NotNull Builder setDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        public @Nullable String getDescription() {
            return description;
        }

        // VERSION //

        public @NotNull Builder setVersion(@NotNull String version) {
            this.version = version;
            return this;
        }

        public String getVersion() {
            return version;
        }

        // WEBSITE //

        public @NotNull Builder setWebsite(@Nullable String website) {
            this.website = website;
            return this;
        }

        public @Nullable String getWebsite() {
            return website;
        }

        // AUTHORS //

        public @NotNull Builder addAuthor(@NotNull String author) {
            this.authors.add(author);
            return this;
        }

        public @NotNull Builder setAuthors(@Nullable Collection<String> authors) {

            this.authors.clear();

            if (authors != null) {
                this.authors.addAll(authors);
            }

            return this;

        }

        public @NotNull List<String> getAuthors() {
            return new ArrayList<>(authors);
        }

        // DEPENDENCIES //

        public @NotNull Builder addDependency(@NotNull ModuleDependency dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        public @NotNull Builder addDependency(@NotNull String id, boolean required, boolean joinClasspath, @NotNull LoadOrder order) {
            this.dependencies.add(new ModuleDependency(id, required, joinClasspath, order));
            return this;
        }

        public @NotNull Builder setDependencies(@Nullable Collection<ModuleDependency> dependencies) {

            this.dependencies.clear();

            if (dependencies != null) {
                this.dependencies.addAll(dependencies);
            }

            return this;

        }

        public @NotNull List<ModuleDependency> getDependencies() {
            return new ArrayList<>(dependencies);
        }

        // LIBRARIES //

        public @NotNull Builder addLibrary(@NotNull LibraryDeclaration library) {
            this.libraries.add(library);
            return this;
        }

        public @NotNull Builder addLibrary(@NotNull String group, @NotNull String artifact, @NotNull String version, @Nullable String source) {
            this.libraries.add(new LibraryDeclaration(group, artifact, version, source));
            return this;
        }

        public @NotNull Builder setLibraries(@Nullable Collection<LibraryDeclaration> libraries) {

            this.libraries.clear();

            if (libraries != null) {
                this.libraries.addAll(libraries);
            }

            return this;

        }

        public @NotNull List<LibraryDeclaration> getLibraries() {
            return new ArrayList<>(libraries);
        }


    }

}
