package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface IModuleManager {

    @NotNull ISBDS getSbds();

    @NotNull File getModulesDir();

    //
    // MODULES
    //

    // LOADING & UNLOADING //

    @NotNull IModule loadModule(@NotNull File file) throws ModuleLoadingException, ModuleRefusedException;


    void unloadModule(@NotNull IModule module) throws ModuleStateCallbackException;

    default void unloadModule(@NotNull ModuleMain moduleMain) throws ModuleStateCallbackException {
        unloadModule(moduleMain.getModule());
    }

    // ENABLING & DISABLING //

    void enableModule(@NotNull IModule module) throws ModuleStateCallbackException, ModuleRefusedException;

    default void enableModule(@NotNull ModuleMain moduleMain) throws ModuleStateCallbackException, ModuleRefusedException {
        enableModule(moduleMain.getModule());
    }


    void disableModule(@NotNull IModule module) throws ModuleStateCallbackException;

    default void disableModule(@NotNull ModuleMain moduleMain) throws ModuleStateCallbackException {
        disableModule(moduleMain.getModule());
    }

    // GETTERS //

    @Nullable IModule getModule(@NotNull String id);

    @NotNull List<IModule> getModules();

    // MISC //

    @NotNull IModule checkModuleValid(@NotNull IModule module);

    @NotNull IModule checkModuleEnabled(@NotNull IModule imodule, @Nullable String message);

    //
    // NAMES
    //

    String ALLOWED_NAME_CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIGKLMNOPQRSTUVWXYZ";

    String ALLOWED_ID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890";

}
