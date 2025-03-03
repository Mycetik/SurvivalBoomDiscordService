package net.survivalboom.sbds.core.libraries.simple;

import net.survivalboom.sbds.core.libraries.JarLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class SimpleLibrariesDownloader {

    private final File dir;

    private final JarLoader jarLoader;


    public SimpleLibrariesDownloader(@NotNull File file, @NotNull JarLoader jarLoader) {

        Objects.requireNonNull(file, "file == null");
        if (file.isFile()) throw new IllegalArgumentException("File object represents an existing file, not a folder. Do you want to fuck yourself?");

        this.dir = file;
        this.jarLoader = jarLoader;

        //noinspection ResultOfMethodCallIgnored
        file.mkdirs();

    }

    public boolean download(@NotNull String repository, @NotNull String group, @NotNull String artifact, @NotNull String version) {
        return download(repository, group, artifact, version, false);
    }

    public boolean download(@NotNull String repository, @NotNull String group, @NotNull String artifact, @NotNull String version, boolean silent) {

        Objects.requireNonNull(repository, "repository == null");
        Objects.requireNonNull(group, "group == null");
        Objects.requireNonNull(artifact, "artifact == null");
        Objects.requireNonNull(version, "version == null");


        String fileName = artifact + "-" + version + ".jar";

        File jarFile = new File(dir, fileName);
        if (jarFile.exists()) {
            mountJar(jarFile, silent);
            return false;
        }

        String repositoryFormatted = repository.endsWith("/") ? repository : repository + "/";
        String url = repositoryFormatted + group.replace(".", "/") + "/" + artifact + "/" + version + "/" + fileName;

        if (!silent) System.out.println("* Downloading " + fileName + "...");

        try {
            try (InputStream in = new URI(url).toURL().openStream()) {
                Files.copy(in, jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        mountJar(jarFile, silent);

        return true;

    }

    private void mountJar(@NotNull File file, boolean silent) {
        jarLoader.mountJar(file);
        if (!silent) System.out.println("+ Mounted " + file.getName());
    }


}
