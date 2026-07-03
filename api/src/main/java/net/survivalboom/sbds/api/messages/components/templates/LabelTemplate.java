package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.components.ModalComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LabelTemplate implements ModalComponentTemplate<Label> {

    private final int index;

    private final String label;

    private final @Nullable String description;

    private final ComponentTemplate<? extends LabelChildComponent> child;


    public LabelTemplate(
            @NotNull String label,
            @Nullable String description,
            int index,
            @NotNull ComponentTemplate<? extends LabelChildComponent> child
    ) {

        Objects.requireNonNull(label, "label == null");
        Objects.requireNonNull(child, "child == null");

        if (label.isBlank()) {
            throw new IllegalArgumentException("label is blank");
        }

        if (description != null && description.isBlank()) {
            throw new IllegalArgumentException("description is blank");
        }

        this.label = label;
        this.description = description;

        this.index = index;

        this.child = child;

    }

    @Override
    public int getRow() {
        return index;
    }

    @Override
    public @NotNull Class<Label> getComponentClass() {
        return Label.class;
    }

    @Override
    public Component.@NotNull Type getType() {
        return Component.Type.LABEL;
    }

    @Override
    public @NotNull Label build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return Label.of(
                StringParser.stParse(parser, label),
                StringParser.stParseNullable(parser, description),
                child.build(parser, null)
        );
    }

}
