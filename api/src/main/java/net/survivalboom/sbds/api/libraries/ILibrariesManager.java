package net.survivalboom.sbds.api.libraries;

import net.survivalboom.sbds.api.modules.IModule;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public interface ILibrariesManager {

    String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";

    static @NotNull String generateArtifactUrl(@NotNull String repository, @NotNull String group, @NotNull String artifact, @NotNull String version) {

        Objects.requireNonNull(repository, "repo == null");
        Objects.requireNonNull(group, "group == null");
        Objects.requireNonNull(artifact, "artifact == null");
        Objects.requireNonNull(version, "version == null");

        String fileName = artifact + "-" + version;
        String repositoryFormatted = repository.endsWith("/") ? repository : repository + "/";

        return repositoryFormatted + group.replace(".", "/") + "/" + artifact + "/" + version + "/" + fileName;

    }

    static @NotNull String generateJarFileName(@NotNull String group, @NotNull String artifact, @NotNull String version, @Nullable String type) {
        String s = group + "." + artifact + "-" + version;
        if (type == null) return s;
        return s + "." + type;
    }

    static @NotNull String generateCompactDependencyString(@NotNull String group, @NotNull String artifact, @NotNull String version) {
        return group + ":" + artifact + ":" + version;
    }


    void download(@NotNull IModule module, @NotNull ConfigurationSection section) throws LibraryParseException, RepositoryConnectionException, IOException, URISyntaxException;

    void download(@NotNull IModule module, @NotNull File file) throws IOException, InvalidConfigurationException, LibraryParseException, RepositoryConnectionException, URISyntaxException;

}
