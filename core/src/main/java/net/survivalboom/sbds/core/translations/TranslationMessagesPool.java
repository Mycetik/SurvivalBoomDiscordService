package net.survivalboom.sbds.core.translations;

import net.survivalboom.sbds.api.messages.template.EmbedMessageTemplate;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.translations.ITranslationsMessagesPool;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TranslationMessagesPool extends Valid implements ITranslationsMessagesPool {

    private final Translation translation;

    protected Registration<ITranslationsMessagesPool> registration;


    private final Map<String, IMessageTemplate> messagesMap = new HashMap<>();


    public TranslationMessagesPool(@NotNull Translation translation) {
        this.translation = translation;
    }


    @Override
    public @NotNull Registration<ITranslationsMessagesPool> getRegistration() {
        return registration;
    }

    @Override
    public @NotNull ITranslation getTranslation() {
        return translation;
    }

    //
    // MESSAGES
    //

    @Override
    public boolean addMessage(@NotNull String key, @NotNull IMessageTemplate template, boolean force) {

        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(template, "template == null");
        checkValid();

        if (!force && messagesMap.containsKey(key)) {
            return false;
        }

        messagesMap.put(key, template);
        translation.cache.put(key, template);

        return true;

    }

    @Override
    public @Nullable IMessageTemplate removeMessage(@NotNull String key) {

        checkValid();

        IMessageTemplate result = messagesMap.remove(key);
        if (result != null) {
            translation.cache.remove(key);
        }

        return result;

    }

    @Override
    public @Nullable IMessageTemplate getMessage(@NotNull String key) {
        checkValid();
        return messagesMap.get(key);
    }

    @Override
    public @NotNull Map<String, IMessageTemplate> getMessages() {
        return new HashMap<>(messagesMap);
    }

    //
    // SAVING/LOADING
    //

    @Override
    public @NotNull LoadResult load(@NotNull ConfigurationSection section, boolean replace) {

        checkValid();

        LoadResult result = new LoadResult(new HashMap<>(), new HashMap<>());
        load0(result, section, "");

        if (replace) {
            this.messagesMap.clear();
        }

        this.messagesMap.putAll(result.loaded());

        return result;

    }

    private void load0(@NotNull LoadResult result, @NotNull ConfigurationSection section, @NotNull String path) {

        for (String key : section.getKeys(false)) {

            String curPath = path + key;

            ConfigurationSection sect = section.getConfigurationSection(key);

            String content = section.getString(key, "null");
            if (sect == null) {
                result.loaded().put(curPath, EmbedMessageTemplate.ofString(content).build());
                continue;
            }

            if (sect.getKeys(false).stream().anyMatch(k -> k.contains("$"))) {

                IMessageTemplate template;

                try {
                    template = IMessageTemplate.fromSection(sect);
                }

                catch (Exception e) {
                    result.failed().put(curPath, e);
                    continue;
                }

                result.loaded().put(curPath, template);

                continue;

            }

            load0(result, sect, curPath + ".");


        }

    }


    @Override
    public void save(@NotNull ConfigurationSection section) {

        for (var entry : messagesMap.entrySet()) {

            String key = entry.getKey();
            IMessageTemplate template = entry.getValue();

            ConfigurationSection sect = section.createSection(key);
            template.dump(sect);

            section.set(key, sect);

        }

    }

}
