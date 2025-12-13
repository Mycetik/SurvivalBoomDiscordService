package net.survivalboom.sbds.core.messages;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.interaction.component.IComponent;
import net.survivalboom.sbds.api.messages.MessageTemplate;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.core.translations.Translation;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class Message implements IMessage {

    private final String key;

    private final Translation translation;

    private final MessageTemplate template;


    public Message(@NotNull String key, @NotNull Translation translation, @NotNull MessageTemplate template) {
        this.key = key;
        this.translation = translation;
        this.template = template;
    }


    @Override
    public @NotNull String key() {
        return key;
    }

    @Override
    public @NotNull Translation translation() {
        return translation;
    }

    @Override
    public void dump(@NotNull ConfigurationSection section) {



    }

    @Override
    public @NotNull MessageTemplate template() {
        return template;
    }

    @Override
    public @NotNull MessageCreateData build(@Nullable Function<IComponent, String> componentIdCreator, @NotNull IMessages messages, @Nullable Placeholders placeholders) {
        Function<String, String> parser = s -> messages.parse(s, key -> messages.getMessage(key, translation, true), placeholders);
        return template.build(componentIdCreator, parser);
    }

    @Override
    public @NotNull String buildString(@Nullable Placeholders placeholders) {
        return template.buildString(placeholders);
    }

}
