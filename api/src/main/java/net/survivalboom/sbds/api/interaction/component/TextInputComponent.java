package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TextInputComponent extends AbstractLabelComponent<TextInputComponent.Builder, TextInputComponent, TextInput> {

    protected final TextInputStyle style;

    protected final int min;

    protected final int max;

    public TextInputComponent(
            @NotNull String name,
            @Nullable String title,
            @Nullable String description,
            @NotNull TextInputStyle style,
            int min,
            int max,
            int row,
            int priority,
            boolean isStatic
    ) {
        super(name, title, description, row, priority, isStatic, Component.Type.TEXT_INPUT);
        this.style = style;
        this.min = min;
        this.max = max;
    }

    @Override
    public @NotNull TextInputComponent.Builder copy() {
        return new Builder(this);
    }

    @Override
    public @NotNull TextInput createComponent(@NotNull Function<String, String> parser, @Nullable Function<IComponent, String> componentIdCreator) {
        return TextInput.create(name, style).setMinLength(min).setMaxLength(max).build();
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull Builder fromSection(@NotNull TypeMap map) throws InvalidComponentException {
        var builder = builder();
        AbstractLabelComponent.fromSection(builder, map);
        return builder;
    }

    public static class Builder extends AbstractLabelComponent.Builder<Builder, TextInputComponent, TextInput> {

        protected TextInputStyle style;

        protected int min;

        protected int max;


        protected Builder() {}

        protected Builder(@NotNull Builder builder) {
            super(builder);
            this.style = builder.style;
            this.min = builder.min;
            this.max = builder.max;
        }

        protected Builder(@NotNull TextInputComponent component) {
            super(component);
            this.style = component.style;
            this.min = component.min;
            this.max = component.max;
        }

        // STYLE //

        public @NotNull Builder setStyle(@NotNull TextInputStyle style) {
            this.style = style;
            return this;
        }

        public @Nullable TextInputStyle getStyle() {
            return style;
        }

        // MIN //

        public @NotNull Builder setMinLength(int min) {
            this.min = min;
            return this;
        }

        public int getMinLength() {
            return min;
        }

        // MAX //

        public @NotNull Builder setMaxLength(int max) {
            this.max = max;
            return this;
        }

        public int getMaxLength() {
            return max;
        }

        //
        // BUILD
        //

        @Override
        public @NotNull TextInputComponent build() {
            return new TextInputComponent(name, title, description, style, min, max, row, priority, isStatic);
        }

        @Override
        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
