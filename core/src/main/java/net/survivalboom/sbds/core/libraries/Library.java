package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.ILibrary;
import net.survivalboom.sbds.api.libraries.IPomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Library implements ILibrary {

    private final IPomData pom;

    private final File file;

    private final DynamicClassLoader classLoader;

    private final List<ILibrary> dependencies = new ArrayList<>();


    public Library(
            @NotNull IPomData pom,
            @NotNull File file,
            @NotNull DynamicClassLoader classLoader,
            @Nullable Collection<ILibrary> dependencies
    ) {

        Objects.requireNonNull(pom, "pom == null");
        Objects.requireNonNull(file, "file == null");
        Objects.requireNonNull(classLoader, "classLoader == null");

        this.pom = pom;
        this.file = file;
        this.classLoader = classLoader;

        if (dependencies != null) {
            this.dependencies.addAll(dependencies);
        }

    }


    @Override
    public @NotNull IPomData getPomData() {
        return pom;
    }

    @Override
    public @NotNull File getFile() {
        return file;
    }

    @Override
    public @NotNull List<ILibrary> getDependencies() {
        return dependencies;
    }

    @Override
    public @NotNull DynamicClassLoader getClassLoader() {
        return classLoader;
    }

}
