package net.survivalboom.sbds.core.libraries.simple;

import net.survivalboom.sbds.core.libraries.DynamicClassLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class SimpleLibrariesDownloader {

    private final File dir;

    private final DynamicClassLoader classLoader;

    private final List<SimpleLibrary> libraries = new ArrayList<>();


    public SimpleLibrariesDownloader(@NotNull File workingDir, @NotNull DynamicClassLoader classLoader) {

        Objects.requireNonNull(workingDir, "workingDir == null");
        Objects.requireNonNull(classLoader, "classLoader == null");
        Objects.requireNonNull(classLoader, "classLoader == null");

        if (workingDir.isFile()) {
            throw new IllegalArgumentException("File object represents an existing file, not a folder. Do you want to fuck yourself?");
        }

        this.dir = workingDir;
        this.classLoader = classLoader;

        //noinspection ResultOfMethodCallIgnored
        workingDir.mkdirs();

    }

    public @NotNull SimpleLibrary download(@NotNull String repository, @NotNull String group, @NotNull String artifact, @NotNull String version) {
        return download(repository, group, artifact, version, false);
    }


    public @NotNull SimpleLibrary download(
        @NotNull String repository,
        @NotNull String group,
        @NotNull String artifact,
        @NotNull String version,
        boolean silent
    ) {

        Objects.requireNonNull(repository, "repository == null");
        Objects.requireNonNull(group, "group == null");
        Objects.requireNonNull(artifact, "artifact == null");
        Objects.requireNonNull(version, "version == null");

        String name = group + "-" + artifact;
        String fileName = name + "-" + version + ".jar";

        File jarFile = new File(dir, fileName);

        DynamicClassLoader libraryClassLoader = new DynamicClassLoader(name, null);
        SimpleLibrary library = new SimpleLibrary(name, group, artifact, version, repository, jarFile, libraryClassLoader, new ArrayList<>());

        if (!jarFile.exists()) {

            String repositoryFormatted = repository.endsWith("/") ? repository : repository + "/";
            String url = repositoryFormatted + group.replace(".", "/") + "/" + artifact + "/" + version + "/" + fileName;

            if (!silent) {
                System.out.println("* Downloading " + fileName + "...");
            }

            try {
                try (InputStream in = new URI(url).toURL().openStream()) {
                    Files.copy(in, jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }

        }

        libraries.add(library);

        libraryClassLoader.addSource(jarFile);
        libraryClassLoader.addClassSupplier(cn -> classLoader.getClass(cn, true, true, true));

        System.out.printf("* Mounted " + fileName);

        return library;

    }


    public @NotNull List<SimpleLibrary> getLibrariesInstalled() {
        return new ArrayList<>(libraries);
    }


}
