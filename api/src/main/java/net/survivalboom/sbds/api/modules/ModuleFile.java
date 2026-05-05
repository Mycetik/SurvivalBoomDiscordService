package net.survivalboom.sbds.api.modules;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.jar.JarFile;

public record ModuleFile(@NotNull File file, @NotNull JarFile jarFile) {}
