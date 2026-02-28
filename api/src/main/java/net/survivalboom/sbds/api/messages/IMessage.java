package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface IMessage {

    @NotNull String key();

    @NotNull ITranslation translation();


    void dump(@NotNull ConfigurationSection section);


    @NotNull MessageTemplate template();

    @NotNull MessageCreateData build(@Nullable Function<Component, String> componentIdCreator, @NotNull IMessages messages, @Nullable Placeholders placeholders);

    @NotNull String buildString(@Nullable Placeholders placeholders);

}
