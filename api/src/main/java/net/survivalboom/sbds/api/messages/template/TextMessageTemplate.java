package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
    public @NotNull MessageCreateBuilder createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return new MessageCreateBuilder().setContent(StringParser.stParse(parser, content));
    }

}
