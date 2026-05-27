package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Objects;

@ConfigSerializable
public class TextMessageTemplate implements IMessageTemplate {

    private final String content;

    public TextMessageTemplate(@NotNull String text) {

        Objects.requireNonNull(text, "text == null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text is empty");
        }

        this.content = text;

    }

    public @NotNull String getContent() {
        return content;
    }

    @Override
    public @NotNull MessageCreateData createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return MessageCreateData.fromContent(Objects.requireNonNull(StringParser.stParseNullable(parser, content)));
    }

}
