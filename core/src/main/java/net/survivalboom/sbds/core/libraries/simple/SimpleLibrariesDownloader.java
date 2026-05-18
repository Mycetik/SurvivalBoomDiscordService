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
        String fileName = artifact + "-" + version + ".jar";

        File jarFile = new File(dir, fileName);

        // Якщо jar файл не існує, завантажуємо з репозиторія.
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

        DynamicClassLoader libraryClassLoader = new DynamicClassLoader(name, null);
        SimpleLibrary library = new SimpleLibrary(name, group, artifact, version, repository, jarFile, libraryClassLoader, new ArrayList<>());

        libraries.add(library);
        libraryClassLoader.addSource(jarFile);

        // 1 - Додаємо головний ClassLoader усього SBDS як джерело для пошуку класів.
        // 2 - Додаємо у головний ClassLoader поточний ClassLoader бібліотеки як джерело для пошуку класів
        // Важливо! Виконуємо пошук тільки по класам самої бібліотеки, не залежностей і не parent!
        libraryClassLoader.addClassSupplier("GLOBAL", cn -> classLoader.getClass(cn, true, true));
        classLoader.addClassSupplier(fileName, cn -> libraryClassLoader.getClass(cn, false, false));

        // Додаємо правильну підтримку SPI //
        libraryClassLoader.addResourceSupplier("SPI", n -> n.startsWith("META-INF/services/"), this::findGlobalSPIMetaInf);

        System.out.println("* Mounted " + fileName);

        return library;

    }

    private List<URL> findGlobalSPIMetaInf(@NotNull String name) {

        List<URL> result = new ArrayList<>();

        for (var lib : libraries) {
            result.addAll(lib.classLoader().findResources(name, false, false));
        }

        return result;

    }


    public @NotNull List<SimpleLibrary> getLibrariesInstalled() {
        return new ArrayList<>(libraries);
    }


}
