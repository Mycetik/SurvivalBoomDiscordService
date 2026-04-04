package net.survivalboom.sbds.core.translations;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.translations.IMessage;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.translations.ITranslation;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Message implements IMessage {

    private final String key;

    private final ITranslation translation;

    private final IMessageTemplate template;

    protected Registration<IMessage> registration;


    public Message(
            @NotNull String key,
            @NotNull ITranslation translation,
            @NotNull IMessageTemplate template
    ) {

        this.key = key;
        this.translation = translation;
        this.template = template;

    }



    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    public @NotNull ITranslation getTranslation() {
        return translation;
    }

    @Override
    public @NotNull Registration<IMessage> getRegistration() {
        return registration;
    }

    @Override
    public void dump(@NotNull ConfigurationSection section) {

    }

    @Override
    public @NotNull IMessageTemplate getTemplate() {
        return template;
    }

    @Override
    public @NotNull MessageCreateData createMessageData(
            @Nullable StringParser parser,
            @Nullable ComponentLinker linker
    ) {
        return template.createMessageData(parser, linker);
    }

}
