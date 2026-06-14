package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TextInputTemplate implements ComponentTemplate<TextInput> {

    private final String name;

    private final int index;


    private final int min;

    private final int max;

    private final boolean required;


    private final @Nullable String placeholder;

    private final @Nullable String value;

    private final TextInputStyle style;


    public TextInputTemplate(
            @NotNull String name,
            int index,

            int min,
            int max,
            boolean required,

            @Nullable String placeholder,
            @Nullable String value,

            @NotNull TextInputStyle style

    ) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(style, "style == null");

        if (style == TextInputStyle.UNKNOWN) {
            throw new IllegalArgumentException("Invalid style `" + style + "`");
        }

        this.name = name;
        this.index = index;

        this.min = min;
        this.max = max;
        this.required = required;

        this.placeholder = placeholder;
        this.value = value;

        this.style = style;

    }

    @Override
    public int getRow() {
        return index;
    }

    // TEXT INPUT //

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean isRequired() {
        return required;
    }


    public @Nullable String getPlaceholder() {
        return placeholder;
    }

    public @Nullable String getValue() {
        return value;
    }


    public @NotNull TextInputStyle getStyle() {
        return style;
    }

    // COMPONENT //

    @Override
    public @NotNull Class<TextInput> getComponentClass() {
        return TextInput.class;
    }

    @Override
    public @NotNull Component.Type getType() {
        return Component.Type.TEXT_INPUT;
    }

    @Override
    public @NotNull TextInput build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return TextInput.create(name, style)
                .setMinLength(min)
                .setMaxLength(max)
                .setRequired(required)
                .setPlaceholder(StringParser.stParseNullable(parser, placeholder))
                .setValue(StringParser.stParseNullable(parser, value))
                .build();
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private int index = 0;


        private int min = 1;

        private int max = -1;

        private boolean required = true;


        private @Nullable String placeholder = null;

        private @Nullable String value = null;

        private TextInputStyle style = TextInputStyle.SHORT;


        private Builder(TextInputTemplate template) {

            this.name = template.name;
            this.index = template.index;

            this.min = template.min;
            this.max = template.max;

            this.required = template.required;

            this.placeholder = template.placeholder;
            this.value = template.value;
            this.style = template.style;

        }

        private Builder(Builder builder) {

            this.name = builder.name;
            this.index = builder.index;

            this.min = builder.min;
            this.max = builder.max;

            this.required = builder.required;

            this.placeholder = builder.placeholder;
            this.value = builder.value;
            this.style = builder.style;

        }

        private Builder() {

        }

        // NAME //

        public @NotNull Builder setName(@NotNull String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        // ROW //

        public @NotNull Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public int getIndex() {
            return index;
        }

        // MIN //

        public @NotNull Builder setMin(int min) {
            this.min = min;
            return this;
        }

        public int getMin() {
            return min;
        }

        // MAX //

        public @NotNull Builder setMax(int max) {
            this.max = max;
            return this;
        }

        public int getMax() {
            return max;
        }

        // REQUIRED //

        public @NotNull Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        // PLACEHOLDER //

        public @NotNull Builder setPlaceholder(@Nullable String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public @Nullable String getPlaceholder() {
            return placeholder;
        }

        // VALUE //

        public @Nullable Builder setValue(@Nullable String value) {
            this.value = value;
            return this;
        }

        public String getValue() {
            return value;
        }

        // STYLE //

        public @NotNull Builder setStyle(@NotNull TextInputStyle style) {
            this.style = style;
            return this;
        }

        public TextInputStyle getStyle() {
            return style;
        }

        // BUILD //

        public @NotNull TextInputTemplate build() {
            return new TextInputTemplate(name, index, min, max, required, placeholder, value, style);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
