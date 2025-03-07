package net.survivalboom.sbds.core.libraries;

import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PomFile(@NotNull LibrarySearchInfo info, @NotNull ConfigurationSection pom) {

    public PomFile {

        Objects.requireNonNull(info, "info == null");
        Objects.requireNonNull(pom, "pom == null");

    }

}
