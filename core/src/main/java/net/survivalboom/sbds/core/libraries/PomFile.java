package net.survivalboom.sbds.core.libraries;

import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PomFile(@NotNull LibrarySearchInfo info, @NotNull String url, @NotNull ConfigurationSection pom, @NotNull String original) {

    public PomFile {

        Objects.requireNonNull(info, "info == null");
        Objects.requireNonNull(pom, "pom == null");
        Objects.requireNonNull(url, "url == null");

    }

}
