package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface IModuleManager {


    String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIGKLMNOPQRSTUVWXYZ";

    static boolean checkNameValid(@NotNull String name) {

        for (char c : name.toCharArray()) {
            if (ALLOWED_CHARACTERS.indexOf(c) == -1) return false;
        }

        return true;

    }


    @Nullable IModule loadModule(@NotNull File file);

    void unloadModule(@NotNull IModule module);

    void enableModule(@NotNull IModule module);

    void disableModule(@NotNull IModule module);


    default void unloadModule(@NotNull ModuleMain moduleMain) {
        unloadModule(moduleMain.getModule());
    }

    default void disableModule(@NotNull ModuleMain moduleMain) {
        disableModule(moduleMain.getModule());
    }



    @Nullable IModule getModule(@NotNull String name);

    @NotNull ISBDS getSbds();

    @NotNull List<IModule> getModules();

    @NotNull File getModulesDir();

}
