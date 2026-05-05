package net.survivalboom.sbds.api.libraries;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public interface ILibrary {

    @NotNull IPomData getPomData();

    @NotNull File getFile();

    @NotNull List<ILibrary> getDependencies();

    @NotNull ClassLoader getClassLoader();

}
