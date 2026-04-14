package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.InvalidComponentException;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.typemap.ModifiableTypeMap;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMessageTemplate {

    @NotNull MessageCreateData createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker);

    void dump(@NotNull ModifiableTypeMap map);


    static @NotNull IMessageTemplate fromSection(@NotNull ConfigurationSection section) throws InvalidComponentException, InvalidEmbedException {

        boolean isEmbedMessage = section.isString("$embed") || section.isString("$embeds") || section.isString("$content");

        if (isEmbedMessage) {
            return EmbedMessageTemplate.ofSection(section).build();
        }

        else {
            return ComponentMessageTemplate.ofSection(section).build();
        }

    }

}
