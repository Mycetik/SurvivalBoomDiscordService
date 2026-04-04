package net.survivalboom.sbds.api.interaction.button;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.survivalboom.sbds.api.messages.components.InvalidComponentException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class ButtonTemplate implements Component {

    private static final Logger log = LoggerFactory.getLogger(ButtonTemplate.class);
    private final String name;

    private final String label;

    private final Emoji emoji;

    private final ButtonStyle style;

    private final int row;

    private final int priority;

    private final boolean isStatic;


    private ButtonTemplate(
            @Nullable String name,
            @Nullable String label,
            @Nullable Emoji emoji,
            @NotNull ButtonStyle style,
            int row,
            int priority,
            boolean isStatic
    ) {

        this.name = name;
        this.label = label;
        this.emoji = emoji;
        this.style = style;
        this.row = row;
        this.priority = priority;
        this.isStatic = isStatic;

        if (label == null && emoji == null) {
            throw new IllegalArgumentException("Emoji and label == null");
        }

    }

    @Override
    public @NotNull ItemComponent build(@NotNull Function<Component, String> componentIdCreator, @NotNull Function<String, String> parser) {
        String id = componentIdCreator.apply(this);
        return Button.of(
                style,
                id,
                label != null ? parser.apply(label) : null,
                emoji
        );
    }

    @Override
    public int row() {
        return row;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public @Nullable String name() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.Component.@NotNull Type type() {
        return net.dv8tion.jda.api.interactions.components.Component.Type.BUTTON;
    }


    public static class Builder {

        private String name;

        private String label;

        private Emoji emoji;

        private ButtonStyle style;

        private int row;

        private int priority;

        private boolean isStatic;

        private Builder(
                @Nullable String name,
                @Nullable String label,
                @Nullable Emoji emoji,
                @Nullable ButtonStyle style,
                int row,
                int priority,
                boolean isStatic
        ) {

            this.name = name;
            this.label = label;
            this.emoji = emoji;
            this.style = style;

            this.row = row;
            this.priority = priority;

            this.isStatic = isStatic;

        }

        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder setLabel(@Nullable String label) {
            this.label = label;
            return this;
        }

        public @NotNull Builder setEmoji(@Nullable Emoji emoji) {
            this.emoji = emoji;
            return this;
        }

        public @NotNull Builder setStyle(@NotNull ButtonStyle style) {
            this.style = style;
            return this;
        }

        public @NotNull Builder setRow(int row) {
            if (row < 1 || row > 5) throw new IllegalArgumentException("Row must be between 1 and 5, got " + row);
            this.row = row;
            return this;
        }

        public @NotNull Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public @NotNull Builder setStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }


        public @NotNull ButtonTemplate build() {
            return new ButtonTemplate(name, label, emoji, style, row, priority, isStatic);
        }

        public @NotNull Builder copy() {
            return new Builder(name, label, emoji, style, row, priority, isStatic);
        }

    }

    public static @NotNull Builder builder() {
        return new Builder(null, null, null, null, 1, 1, false);
    }

    public static @NotNull ButtonTemplate create(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String name = typeMap.get("name", String.class);
        boolean isStatic = Boolean.TRUE.equals(typeMap.get("static", Boolean.class));

        String label = typeMap.get("label", String.class);

        String emojiRaw = typeMap.get("emoji", String.class);

        int row = typeMap.get("row", 1);
        int priority = typeMap.get("priority", 1);
        String styleRaw = typeMap.get("style", String.class);

        Emoji emoji = emojiRaw == null ? null : Emoji.fromFormatted(emojiRaw);

        if (label == null && emoji == null) throw new InvalidComponentException("Button label and button emoji are null!");

        ButtonStyle style = CommonUtils.getEnumValue(ButtonStyle.class, styleRaw);
        if (style == null) throw new InvalidComponentException("Invalid button style `" + styleRaw + "`");

        return builder()
                .setName(name)
                .setLabel(label)
                .setRow(row)
                .setPriority(priority)
                .setStatic(isStatic)
                .setEmoji(emoji)
                .setStyle(style)
                .build();

    }

}
