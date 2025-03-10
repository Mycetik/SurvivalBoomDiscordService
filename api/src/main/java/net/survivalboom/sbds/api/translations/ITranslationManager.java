package net.survivalboom.sbds.api.translations;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ITranslationManager {

    @Nullable ITranslation getTranslation(@NotNull String name);


    @Nullable ITranslation defaultTranslation();

    @Nullable ITranslation fallbackTranslation();

    default void addModuleTranslations(@NotNull ModuleMain moduleMain) {
        addModuleTranslations(moduleMain.getModule());
    }

    void addModuleTranslations(@NotNull IModule module);

}
