package net.survivalboom.sbds.api.translations;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.registrations.Registration;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMessage {

    @NotNull String getKey();

    @NotNull ITranslation getTranslation();

    @NotNull Registration<IMessage> getRegistration();


    void dump(@NotNull ConfigurationSection section);


    @NotNull IMessageTemplate getTemplate();

    @NotNull MessageCreateData createMessageData(
            @Nullable StringParser parser,
            @Nullable ComponentLinker linker
    );

}
