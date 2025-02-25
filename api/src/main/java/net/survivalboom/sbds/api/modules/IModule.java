package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.jar.JarFile;

public interface IModule {

    @NotNull String getName();


    boolean isEnabled();


    @NotNull File getFile();

    @NotNull JarFile getJar();

    @NotNull IModuleManager getModuleManager();


    @NotNull Logger getLogger();

    @NotNull IModuleMeta getMeta();

    @NotNull IModuleClassLoader getClassLoader();

    @NotNull ModuleMain getMain();


    @NotNull ISBDS getSbds();

}
