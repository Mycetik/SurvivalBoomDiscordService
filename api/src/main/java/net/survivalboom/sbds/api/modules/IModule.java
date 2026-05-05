package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;

public interface IModule extends IValid {

    // MODULE INFO //

    @NotNull String getName();

    @NotNull ModuleMeta getMeta();

    boolean isEnabled();

    @NotNull Logger getLogger();

    // MODULE LOCATION //

    @Nullable ModuleFile getFile();

    @NotNull File getDataFolder();

    // MODULE CLASS //

    @NotNull ModuleMain getMain();

}
