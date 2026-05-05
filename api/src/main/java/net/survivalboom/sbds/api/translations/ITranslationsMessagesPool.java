package net.survivalboom.sbds.api.translations;

import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public interface ITranslationsMessagesPool extends IValid {

    @NotNull Registration<ITranslationsMessagesPool> getRegistration();

    @NotNull ITranslation getTranslation();

    //
    // MESSAGES
    //

    boolean addMessage(@NotNull String key, @NotNull IMessageTemplate template, boolean force);

    @Nullable IMessageTemplate removeMessage(@NotNull String key);

    @Nullable IMessageTemplate getMessage(@NotNull String key);

    @NotNull Map<String, IMessageTemplate> getMessages();

    //
    // SAVING/LOADING
    //

    @NotNull LoadResult load(@NotNull ConfigurationNode section, boolean replace);

    default @NotNull LoadResult load(@NotNull File file, boolean replace) throws IOException {

        Objects.requireNonNull(file, "file == null");

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(file.toPath())
                .build();

        ConfigurationNode node = loader.load();

        return load(node, replace);

    }

    void save(@NotNull ConfigurationNode section) throws SerializationException;

    default void save(@NotNull File file) throws IOException {

        Objects.requireNonNull(file, "file == null");

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(file.toPath())
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        ConfigurationNode node = loader.createNode();
        save(node);

    }

    record LoadResult(@NotNull Map<String, IMessageTemplate> loaded, @NotNull Map<String, Exception> failed) {}

}
