package net.survivalboom.sbds.core.libraries.simple;

import net.survivalboom.sbds.core.libraries.DynamicClassLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public record SimpleLibrary(
        @NotNull String name,
        @NotNull String group,
        @NotNull String artifact,
        @NotNull String version,
        @NotNull String repository,
        @NotNull File file,
        @NotNull DynamicClassLoader classLoader,
        @NotNull List<SimpleLibrary>  dependencies
) {}
