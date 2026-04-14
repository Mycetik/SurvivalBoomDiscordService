package net.survivalboom.sbds.api.translations;

import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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

    @NotNull LoadResult load(@NotNull ConfigurationSection section, boolean replace);

    default @NotNull LoadResult load(@NotNull File file, boolean replace) throws IOException, InvalidConfigurationException {

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);

        return load(yamlConfiguration, replace);

    }

    void save(@NotNull ConfigurationSection section);

    default void save(@NotNull File file) throws IOException {

        YamlConfiguration configuration = new YamlConfiguration();
        save(configuration);

        configuration.save(file);

    }

    record LoadResult(@NotNull Map<String, IMessageTemplate> loaded, @NotNull Map<String, Exception> failed) {}

}
