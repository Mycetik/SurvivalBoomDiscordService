package net.survivalboom.sbds.api.translations;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface ITranslationManager {

    //
    // MANAGER
    //

    void reload(@NotNull IModule module);

    default void reload(@NotNull ModuleMain module) {
        reload(module.getModule());
    }

    //
    // TRANSLATIONS MANAGEMENT
    //

    // REGISTRATION //

    @NotNull ITranslation createTranslation(@NotNull IModule module, @NotNull String name);

    default @NotNull ITranslation createTranslation(@NotNull ModuleMain module, @NotNull String name) {
        return createTranslation(module.getModule(), name);
    }

    // LOAD //

    @NotNull ITranslation loadTranslation(@NotNull IModule module, @NotNull ConfigurationNode section) throws InvalidTranslationException;

    @NotNull ITranslation loadTranslation(@NotNull IModule module, @NotNull File file) throws ConfigurateException, InvalidTranslationException;

    // REMOVING //

    boolean removeTranslation(@NotNull ITranslation translation);

    default @Nullable ITranslation removeTranslation(@NotNull NamespacedKey key) {

        ITranslation translation = getTranslation(key);
        if (translation == null) {
            return null;
        }

        removeTranslation(translation);

        return translation;

    }

    default @Nullable ITranslation removeTranslation(@NotNull String name) {
        return removeTranslation(NamespacedKey.fromString(name));
    }

    // GETTERS //

    @Nullable ITranslation getTranslation(@NotNull NamespacedKey key);

    default @Nullable ITranslation getTranslation(@NotNull String key) {
        return getTranslation(NamespacedKey.fromString(key));
    }

    @NotNull List<ITranslation> getTranslations();

    //
    // FALLBACK
    //

    // default translation //

    @Nullable ITranslation getDefaultTranslation();

    void setDefaultTranslation(@Nullable ITranslation translation);

    // fallback translation //

    @Nullable ITranslation getFallbackTranslation();

    void setFallbackTranslation(@Nullable ITranslation translation);

    //
    // MODULES MESSAGES
    //

    void importModuleMessages(@NotNull IModule module, @NotNull String dirName);

    default void importModuleMessages(@NotNull IModule module) {
        importModuleMessages(module, "messages");
    }

    default void importModuleMessages(@NotNull ModuleMain moduleMain, @NotNull String dirName) {
        importModuleMessages(moduleMain.getModule(), dirName);
    }

    default void importModuleMessages(@NotNull ModuleMain moduleMain) {
        importModuleMessages(moduleMain.getModule());
    }

    //
    // DISCORD LOCALE
    //

    @Nullable ITranslation findTranslationByLocale(@NotNull DiscordLocale locale);

    @NotNull Set<DiscordLocale> getAvailableLocales();

}
