package net.survivalboom.sbds.core.translations;

import net.survivalboom.sbds.api.messages.template.EmbedMessageTemplate;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.translations.ITranslationsMessagesPool;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

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
    public @NotNull LoadResult load(@NotNull ConfigurationNode section, boolean replace) {

        checkValid();

        LoadResult result = new LoadResult(new HashMap<>(), new HashMap<>());
        load0(result, section);

        if (replace) {
            this.messagesMap.clear();
        }

        this.messagesMap.putAll(result.loaded());

        return result;

    }

    private void load0(@NotNull LoadResult result, @NotNull ConfigurationNode node) {

        for (ConfigurationNode child : node.childrenMap().values()) {

            String curPath = child.path().toString();

            if (child.isMap()) {

                boolean hasSpecialKey = child.childrenMap().keySet()
                        .stream()
                        .anyMatch(k -> String.valueOf(k).contains("$"));

                if (hasSpecialKey) {

                    try {

                        IMessageTemplate template = IMessageTemplate.fromSection(child);

                        result.loaded().put(curPath, template);

                    }

                    catch (Exception e) {
                        result.failed().put(curPath, e);
                    }

                }

                else {
                    load0(result, child);
                }

            }

            else {
                String content = child.getString("null");
                result.loaded().put(curPath, EmbedMessageTemplate.ofString(content).build());
            }

        }
    }


    @Override
    public void save(@NotNull ConfigurationNode section) throws SerializationException {

        for (var entry : messagesMap.entrySet()) {

            String key = entry.getKey();
            IMessageTemplate template = entry.getValue();

            String[] path = key.split("\\.");
            ConfigurationNode targetNode = section.node((Object[]) path);

            targetNode.set(template);

        }

    }

}
