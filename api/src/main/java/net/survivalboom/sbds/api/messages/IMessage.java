package net.survivalboom.sbds.api.messages;

import net.survivalboom.sbds.api.translations.ITranslation;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMessage {

    @Nullable String key();

    @Nullable String text();

    @Nullable List<IEmbedTemplate> embeds();

    @Nullable ITranslation translation();


    void dump(@NotNull ConfigurationSection cfg);

}
