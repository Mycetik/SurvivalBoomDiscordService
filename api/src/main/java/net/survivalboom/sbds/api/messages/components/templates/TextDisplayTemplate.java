package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TextDisplayTemplate implements ComponentTemplate<TextDisplay> {

    private final int index;

    private final String content;


    public TextDisplayTemplate(
            @NotNull String content,
            int index
    ) {

        Objects.requireNonNull(content, "content == null");
        if (content.isBlank()) {
            throw new IllegalArgumentException("content is blank");
        }

        this.content = content;
        this.index = index;

    }


    public @NotNull String getContent() {
        return content;
    }


    @Override
    public int getRow() {
        return index;
    }

    // COMPONENT //

    @Override
    public @NotNull Class<TextDisplay> getComponentClass() {
        return TextDisplay.class;
    }

    @Override
    public Component.@NotNull Type getType() {
        return Component.Type.TEXT_DISPLAY;
    }

    @Override
    public @NotNull TextDisplay build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return TextDisplay.of(content);
    }

}
