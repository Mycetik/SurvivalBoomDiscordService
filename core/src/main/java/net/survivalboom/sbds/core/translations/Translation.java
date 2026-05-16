package net.survivalboom.sbds.core.translations;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.RegistrationManager;
import net.survivalboom.sbds.api.translations.*;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Valid;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Translation extends Valid implements ITranslation, RegistrationManager.Callback<ITranslationsMessagesPool> {

    private final ITranslationManager manager;

    protected Registration<ITranslation> registration;


    private DiscordLocale discordLocale;

    private String displayName;

    private String icon;


    private final InternalRegistrationManager<ITranslationsMessagesPool> registry;

    protected final Map<String, IMessageTemplate> cache = new HashMap<>();


    public Translation(@NotNull String name, @NotNull TranslationManager manager) {
        this.manager = manager;
        this.registry = new InternalRegistrationManager<>(this, name, this, manager.sbds.getRegistrationRegistry());
        this.registry.init();
    }

    //
    // INFO
    //

    @Override
    public @NotNull Registration<ITranslation> getRegistration() {
        return registration;
    }

    //
    // MESSAGES
    //

    @Override
    public @Nullable IMessageTemplate getMessage(@NotNull String key) {
        checkValid();
        return cache.get(key);
    }

    @Override
    public @NotNull Map<String, IMessageTemplate> getMessages() {
        return new HashMap<>(cache);
    }

    //
    // POOLS
    //

    // create/remove //

    @Override
    public @NotNull ITranslationsMessagesPool createMessagesPool(@NotNull IModule module, @NotNull String name) {
        Objects.requireNonNull(module, "module == null");
        return createMessagesPool0(module, name);
    }

    public @NotNull ITranslationsMessagesPool createMessagesPool0(@Nullable IModule module, @NotNull String name) {

        Objects.requireNonNull(name, "name == null");
        checkValid();

        TranslationMessagesPool pool = new TranslationMessagesPool(this);
        pool.registration = registry.register0(module, name, pool);

        return pool;

    }

    @Override
    public boolean removeMessagesPool(@NotNull ITranslationsMessagesPool pool) {

        checkValid();

        boolean result = registry.unregister(pool) != null;
        if (result) {
            cacheFullRecalculate();
        }

        return result;

    }

    private void cacheFullRecalculate() {

        Map<String, IMessageTemplate> out = new HashMap<>();
        for (var reg : getMessagePools()) {

            var pool = reg.object();
            var m = pool.getMessages();

            out.putAll(m);

        }

        this.cache.clear();
        this.cache.putAll(out);

    }

    // getters //

    @Override
    public @Nullable Registration<ITranslationsMessagesPool> getMessagesPool(@NotNull NamespacedKey key) {
        checkValid();
        return registry.getRegistration(key);
    }

    @Override
    public @NotNull List<Registration<ITranslationsMessagesPool>> getMessagePools() {
        checkValid();
        return registry.getRegistrations();
    }

    //
    // PROPERTIES
    //

    // DISPLAY NAME //

    @Override
    public @Nullable String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(@Nullable String displayName) {
        checkValid();
        this.displayName = displayName;
    }

    // DISCORD LOCALE //

    @Override
    public @Nullable DiscordLocale getDiscordLocale() {
        return discordLocale;
    }

    @Override
    public void setDiscordLocale(@Nullable DiscordLocale locale) {
        checkValid();
        this.discordLocale = locale;
    }

    // ICON //

    @Override
    public @Nullable String getIconEmoji() {
        return icon;
    }

    @Override
    public void setIconEmoji(@Nullable String icon) {
        checkValid();
        this.icon = icon;
    }

    //
    // MISC
    //


    @Override
    public @NotNull ITranslationManager getManager() {
        return manager;
    }

    @Override
    public void remove() {
        checkValid();
        manager.removeTranslation(this);
    }

    @Override
    protected void setValid(boolean v) {

        if (v) {
            registry.init();
        }

        else {
            registry.shutdown();
            cache.clear();
        }

        super.setValid(v);

    }

    @Override
    public String toString() {
        return String.format(
                "Translation{key=%s, displayName=%s, messagesSize=%s}",
                registration.key(),
                displayName,
                cache.size()
        );
    }

}
