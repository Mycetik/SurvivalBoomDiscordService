package net.survivalboom.sbds.api.libraries;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ArtifactAddress(@NotNull String group, @NotNull String artifact, @NotNull String version) {

    public @NotNull String toGradleString() {
        return toGradleString(DEFAULT_GRADLE_SEPARATOR);
    }

    public @NotNull String toGradleString(@NotNull String separator) {
        return group + separator + artifact + separator + version;
    }

    public @NotNull String createRepositoryAddress(@NotNull String repository, @NotNull String fileType) {

        if (!repository.endsWith("/")) {
            repository += "/";
        }

        return repository + group.replace(".", "/") + "/" + artifact + "/" + version + "/" + artifact + "-" + version + "." + fileType;

    }

    @Override
    public String toString() {
        return toGradleString();
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof ArtifactAddress(String group1, String artifact1, String version1))) {
            return false;
        }

        return group.equals(group1) && artifact.equals(artifact1) && version.equals(version1);

    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifact, version);
    }

    //
    // STATIC
    //

    public static final String DEFAULT_FILESYSTEM_SEPARATOR = "$";

    public static final String DEFAULT_GRADLE_SEPARATOR = ":";


    public static @NotNull ArtifactAddress fromDeclaration(@NotNull LibraryDeclaration declaration) {

        String group, artifact, version;

        group = declaration.group();
        artifact = declaration.artifact();
        version = declaration.version();

        return new ArtifactAddress(group, artifact, version);

    }

    public static @NotNull ArtifactAddress fromGradleString(@NotNull String string) {
        return fromGradleString(string, DEFAULT_GRADLE_SEPARATOR);
    }

    public static @NotNull ArtifactAddress fromGradleString(@NotNull String string, @NotNull String separator) {

        Objects.requireNonNull(string, "string == null");
        Objects.requireNonNull(separator, "separator == null");

        String[] parts = separator.split(separator);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid format; Separator: `" + separator + "`; Expected length: 3, Got " + parts.length + "; " + string);
        }

        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];

        return new ArtifactAddress(group, artifact, version);

    }

}
