package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.survivalboom.sbds.api.interaction.modal.IModalComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TextDisplayComponent extends AbstractInteractionComponent<TextDisplayComponent.Builder, TextDisplayComponent, TextDisplay> implements IModalComponent {

    protected final String text;

    public TextDisplayComponent(
            @NotNull String name,
            @NotNull String text,
            int row,
            int priority,
            boolean isStatic
    ) {
        super(name, row, priority, isStatic, Component.Type.TEXT_DISPLAY);
        this.text = text;
    }

    @Override
    public @NotNull TextDisplayComponent.Builder copy() {
        return new Builder(this);
    }

    @Override
    public @NotNull TextDisplay createComponent(@NotNull Function<String, String> parser, @Nullable Function<IComponent, String> componentIdCreator) {
        return TextDisplay.of(parser.apply(text));
    }

    @Override
    public @NotNull ModalTopLevelComponent createModalComponent(@NotNull Function<String, String> parser) {
        return createComponent(parser, null);
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractInteractionComponent.Builder<Builder, TextDisplayComponent, TextDisplay> {

        protected String text;

        protected Builder() {}

        protected Builder(@NotNull Builder builder) {
            super(builder);
            this.text = builder.text;
        }

        protected Builder(@NotNull TextDisplayComponent component) {
            super(component);
            this.text = component.text;
        }

        // TEXT //

        public @NotNull Builder setText(@NotNull String text) {
            this.text = text;
            return this;
        }

        public @Nullable String getText() {
            return text;
        }

        //
        // BUILD
        //

        @Override
        public @NotNull TextDisplayComponent build() {
            return new TextDisplayComponent(name, text, row, priority, isStatic);
        }

        @Override
        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
