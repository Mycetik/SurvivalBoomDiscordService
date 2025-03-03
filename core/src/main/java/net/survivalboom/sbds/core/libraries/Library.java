package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.ILibrary;
import net.survivalboom.sbds.api.libraries.LibraryDownloadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Library implements ILibrary {

    private final LibrariesManager manager;

    private final File file;

    private final String url;


    private final String group;

    private final String artifact;


    private final String name;

    private final String description;

    private final String version;


    private final JSONObject raw;

    private final List<Library> dependencies;


    public Library(@NotNull LibrariesManager manager, @NotNull File file, @NotNull String url, @NotNull String group, @NotNull String artifact, @NotNull String version, @NotNull List<Library> dependencies, @NotNull JSONObject raw) {

        this.manager = manager;

        this.file = file;
        this.url = url;

        this.group = group;
        this.artifact = artifact;
        this.version = version;

        this.dependencies = dependencies;
        this.raw = raw;

        this.name = getStringOrNull("name");
        this.description = getStringOrNull("description");

    }


    public void download() throws LibraryDownloadException {
        for (Library library : dependencies) library.download();
        if (installed()) return;
        manager.downloadJar(this);
    }


    public boolean installed() {
        return file.exists();
    }


    public @NotNull JSONObject getRaw() {
        return new JSONObject(raw);
    }


    public @NotNull String getUrl() {
        return url;
    }

    public @NotNull File getFile() {
        return file;
    }

    public @NotNull String getGroup() {
        return group;
    }

    public @NotNull String getArtifact() {
        return artifact;
    }

    public @NotNull String getVersion() {
        return version;
    }


    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public @NotNull List<Library> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    public @NotNull LibrariesManager getManager() {
        return manager;
    }

    private @Nullable String getStringOrNull(@NotNull String str) {

        try {
            return raw.getString(str);
        }

        catch (JSONException e) {
            return null;
        }

    }

    @Override
    public String toString() {
        return String.format("Library{group=%s, name=%s, version=%s, description=%s, dependencies=%s}", group, name, version, description, dependencies);
    }

}
