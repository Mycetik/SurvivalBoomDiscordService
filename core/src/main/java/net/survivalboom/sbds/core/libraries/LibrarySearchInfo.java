package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.ILibrariesManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record LibrarySearchInfo(String group, String artifact, String version, List<String> repositories) {

    public LibrarySearchInfo(@NotNull String group, @NotNull String artifact, @NotNull String version, @NotNull List<String> repositories) {

        Objects.requireNonNull(group, "group == null");
        Objects.requireNonNull(artifact, "artifact == null");
        Objects.requireNonNull(version, "version == null");
        Objects.requireNonNull(repositories, "repositories == null");

        this.repositories = repositories;
        this.group = group;
        this.artifact = artifact;
        this.version = version;

    }

    @Override
    public String toString() {
        return ILibrariesManager.generateCompactDependencyString(group, artifact, version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositories, group, artifact, version);
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof LibrarySearchInfo(String group1, String artifact1, String version1, List<String> repositories1))) {
            return false;
        }

        return repositories1.equals(repositories) && version1.equals(version) && group1.equals(group) && artifact1.equals(artifact);

    }
}
