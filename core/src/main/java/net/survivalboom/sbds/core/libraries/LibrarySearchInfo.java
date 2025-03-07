package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.ILibrariesManager;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LibrarySearchInfo {

    private final String group;

    private final String artifact;

    private final String version;

    private final List<String> repositories;


    private final String jarFileName;

    private final String pomFileName;

    private final String pomUrlFileName;

    public LibrarySearchInfo(@NotNull String group, @NotNull String artifact, @NotNull String version, @NotNull List<String> repositories) {

        Objects.requireNonNull(group, "group == null");
        Objects.requireNonNull(artifact, "artifact == null");
        Objects.requireNonNull(version, "version == null");
        Objects.requireNonNull(repositories, "repositories == null");

        this.repositories = repositories;
        this.group = group;
        this.artifact = artifact;
        this.version = version;

        String fileName = group + "." + artifact + "-" + version;
        this.jarFileName = fileName + ".jar";
        this.pomFileName = fileName + ".pom";
        this.pomUrlFileName = fileName + ".pom.url";

    }

    public @NotNull String url(@NotNull String repository) {

        Objects.requireNonNull(repository, "repo == null");

        String fileName = artifact + "-" + version;
        String repositoryFormatted = repository.endsWith("/") ? repository : repository + "/";

        return repositoryFormatted + group.replace(".", "/") + "/" + artifact + "/" + version + "/" + fileName;

    }

    public @NotNull String urlPom(@NotNull String repository) {
        return url(repository) + ".pom";
    }

    public @NotNull String urlJar(@NotNull String repository) {
        return url(repository) + ".jar";
    }


    public @NotNull String gradle() {
        return group + ":" + artifact + ":" + version;
    }


    public @NotNull String group() {
        return group;
    }

    public @NotNull String artifact() {
        return artifact;
    }

    public @NotNull String version() {
        return version;
    }

    public @NotNull List<String> repositories() {
        return new ArrayList<>(repositories);
    }


    public @NotNull String jarFileName() {
        return jarFileName;
    }

    public @NotNull String pomFileName() {
        return pomFileName;
    }

    public @NotNull String pomUrlFileName() {
        return pomUrlFileName;
    }


    public @NotNull File jarFile(@NotNull File dir) {
        return new File(dir, jarFileName);
    }

    public @NotNull File pomFile(@NotNull File dir) {
        return new File(dir, pomFileName);
    }

    public @NotNull File pomUrlFile(@NotNull File dir) {
        return new File(dir, pomUrlFileName);
    }



    @Override
    public String toString() {
        return "LibrarySearchInfo{" + gradle() + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositories, group, artifact, version);
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof LibrarySearchInfo searchInfo)) {
            return false;
        }

        return searchInfo.repositories.equals(repositories) && searchInfo.version.equals(version) && searchInfo.group.equals(group) && searchInfo.artifact.equals(artifact);

    }

    //
    // STATIC
    //

    public static @NotNull LibrarySearchInfo create(@NotNull ConfigurationSection section) throws LibrarySectionParseException {

        List<String> repositories = section.getStringList("repositories");
        if (repositories.isEmpty()) repositories.add(ILibrariesManager.MAVEN_CENTRAL_URL);

        String gradle = section.getString("gradle");
        if (gradle != null) {
            return create(repositories, gradle);
        }

        String group = section.getString("group");
        String artifact = section.getString("artifact");
        String version = section.getString("version");

        if (group == null || artifact == null || version == null) throw new LibrarySectionParseException("Invalid library section");

        return new LibrarySearchInfo(group, artifact, version, repositories);

    }

    public static @NotNull LibrarySearchInfo create(@NotNull List<String> repositories, @NotNull String gradle) throws LibrarySectionParseException {

        Objects.requireNonNull(repositories, "repositories == null");
        Objects.requireNonNull(gradle, "gradle == null");

        if (repositories.isEmpty()) throw new LibrarySectionParseException("No repositories provided. (repositories.isEmpty() == true)");

        String[] args = gradle.split(":");
        if (args.length != 3) throw new LibrarySectionParseException("Invalid gradle format `" + gradle + "`.");

        return new LibrarySearchInfo(args[0], args[1], args[2], new ArrayList<>(repositories));

    }

}
