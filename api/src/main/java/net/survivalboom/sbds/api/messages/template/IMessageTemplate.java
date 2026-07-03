package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public interface IMessageTemplate {

    @NotNull MessageCreateBuilder createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker);


    static @NotNull IMessageTemplate fromSection(@NotNull ConfigurationNode section) throws SerializationException {

        boolean hasContent = section.hasChild("$content");
        boolean hasEmbeds = section.hasChild("$embed") || section.hasChild("$embeds");
        boolean hasComponents = section.hasChild("$components");

        String content = section.getString();

        if (hasComponents || hasContent || hasEmbeds) {
            return EmbedMessageTemplate.fromSection(section).build();
        }

        else if (content != null) {
            return new TextMessageTemplate(content);
        }

        else {
            throw new IllegalArgumentException("Unknown message template");
        }

        // TODO Component API v2 все ще не реалізовано :(
//        IMessageTemplate template;
//        if (hasEmbeds) {
//            template = EmbedMessageTemplate.fromSection(section).build();
//        }
//
//        else if (hasComponents) {
//            template = section.get(ComponentMessageTemplate.class);
//        }
//
//        else if (hasContent) {
//            template = new TextMessageTemplate(section.node("$content").getString("null"));
//        }
//
//        else if (content != null) {
//            template = new TextMessageTemplate(content);
//        }
//
//        else {
//            throw new IllegalArgumentException("Unknown message template");
//        }

//        Objects.requireNonNull(template, "template == null; something went wrong?");
//
//        return template;

    }

}
