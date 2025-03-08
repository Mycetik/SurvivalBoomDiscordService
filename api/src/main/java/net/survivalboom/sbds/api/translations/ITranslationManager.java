package net.survivalboom.sbds.api.translations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ITranslationManager {

    @Nullable ITranslation getTranslation(@NotNull String name);


    @Nullable ITranslation defaultTranslation();

    @Nullable ITranslation fallbackTranslation();

}
