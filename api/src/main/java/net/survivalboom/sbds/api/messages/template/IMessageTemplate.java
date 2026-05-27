package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Objects;

public interface IMessageTemplate {

    @NotNull MessageCreateData createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker);


    static @NotNull IMessageTemplate fromSection(@NotNull ConfigurationNode section) throws SerializationException {

        boolean hasContent = section.hasChild("$content");
        boolean hasEmbeds = section.hasChild("$embed") || section.hasChild("$embeds");
        boolean hasComponents = section.hasChild("$components");

        String content = section.getString();

        IMessageTemplate template;

        if (hasEmbeds) {
            template = EmbedMessageTemplate.fromSection(section).build();
        }

        else if (hasComponents) {
            template = section.get(ComponentMessageTemplate.class);
        }

        else if (hasContent) {
            template = new TextMessageTemplate(section.node("$content").getString("null"));
        }

        else if (content != null) {
            template = new TextMessageTemplate(content);
        }

        else {
            throw new IllegalArgumentException("Unknown message template");
        }

        Objects.requireNonNull(template, "template == null; something went wrong?");

        return template;

    }

}
