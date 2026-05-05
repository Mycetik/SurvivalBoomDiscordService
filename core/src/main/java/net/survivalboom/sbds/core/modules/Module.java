package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.modules.*;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.api.utils.valid.Valid;
import net.survivalboom.sbds.core.libraries.DynamicClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Module extends Valid implements IModule {

    private final @Nullable ModuleFile file;

    private final File dataDir;


    private final Logger logger;


    private final ModuleMeta meta;

    private final DynamicClassLoader classLoader;

    protected ModuleMain moduleMain;


    protected boolean enabled = false;


    public Module(
            @NotNull ModuleMeta meta,
            @Nullable ModuleFile file,
            @NotNull File dataDir,
            @NotNull ModuleManager moduleManager
    ) {

        this.meta = meta;
        this.file = file;
        this.dataDir = dataDir;

        this.logger = LoggerFactory.getLogger(meta.getName());

        this.classLoader = new DynamicClassLoader("Module-" + meta.getName(), Module.class.getClassLoader());

    }


    //
    // GETTERS
    //

    // MODULE INFO //

    @Override
    public @NotNull String getName() {
        return meta.getName();
    }

    @Override
    public @NotNull ModuleMeta getMeta() {
        return meta;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    // MODULE LOCATION //

    @Override
    public @Nullable ModuleFile getFile() {
        return file;
    }

    @Override
    public @NotNull File getDataFolder() {
        return dataDir;
    }

    // MODULE CLASS //

    @Override
    public @NotNull ModuleMain getMain() {
        return moduleMain;
    }

    public @NotNull DynamicClassLoader getClassLoader() {
        return classLoader;
    }

    //
    // MISC
    //

    @Override
    public String toString() {
        return meta.getName() + "@" + meta.getVersion();
    }

}
