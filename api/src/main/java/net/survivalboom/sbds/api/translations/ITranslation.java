package net.survivalboom.sbds.api.translations;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface ITranslation extends IValid {

    //
    // INFO
    //

    @NotNull Registration<ITranslation> getRegistration();

    //
    // MESSAGES
    //

    @Nullable IMessageTemplate getMessage(@NotNull String key);

    @NotNull Map<String, IMessageTemplate> getMessages();

    //
    // MESSAGE POOLS
    //

    // CREATION //

    @NotNull ITranslationsMessagesPool createMessagesPool(@NotNull IModule module, @NotNull String name);

    default @NotNull ITranslationsMessagesPool createMessagesPool(@NotNull ModuleMain module, @NotNull String name) {
        return createMessagesPool(module.getModule(), name);
    }

    default @NotNull ITranslationsMessagesPool getOrCreateMessagesPool(@NotNull IModule module, @NotNull String name) {

        var reg = getMessagesPool(NamespacedKey.fromModule(module, name));
        if (reg == null) {
            return createMessagesPool(module, name);
        }

        return reg.object();

    }

    // REMOVE //

    boolean removeMessagesPool(@NotNull ITranslationsMessagesPool pool);

    default @Nullable ITranslationsMessagesPool removeMessagesPool(@NotNull NamespacedKey key) {

        var pool = getMessagesPool(key);
        if (pool == null) {
            return null;
        }

        removeMessagesPool(pool.object());

        return pool.object();

    }

    default @Nullable ITranslationsMessagesPool removeMessagesPool(@NotNull String key) {
        return removeMessagesPool(NamespacedKey.fromString(key));
    }

    // GETTERS //

    @Nullable Registration<ITranslationsMessagesPool> getMessagesPool(@NotNull NamespacedKey key);

    default @Nullable Registration<ITranslationsMessagesPool> getMessagesPool(@NotNull String key) {
        return getMessagesPool(NamespacedKey.fromString(key));
    }

    @NotNull List<Registration<ITranslationsMessagesPool>> getMessagePools();

    //
    // PROPERTIES
    //

    // DISPLAY NAME //

    @Nullable String getDisplayName();

    void setDisplayName(@Nullable String displayName);

    // DISCORD LOCALE //

    @Nullable DiscordLocale getDiscordLocale();

    void setDiscordLocale(@Nullable DiscordLocale locale);

    // ICON //

    @Nullable String getIconEmoji();

    void setIconEmoji(@Nullable String icon);

    //
    // MISC
    //

    @NotNull ITranslationManager getManager();

    void remove();

}
