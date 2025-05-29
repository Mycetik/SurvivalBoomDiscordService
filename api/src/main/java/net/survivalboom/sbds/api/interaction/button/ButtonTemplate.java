package net.survivalboom.sbds.api.interaction.button;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.survivalboom.sbds.api.messages.Component;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public class ButtonTemplate implements Component {

    private final String name;

    private final String label;

    private final Emoji emoji;

    private final ButtonStyle style;

    private final int row;

    private final int priority;


    private ButtonTemplate(
            @Nullable String name,
            @NotNull String label,
            @Nullable Emoji emoji,
            @NotNull ButtonStyle style,
            int row,
            int priority
    ) {

        this.name = name;
        this.label = label;
        this.emoji = emoji;
        this.style = style;
        this.row = row;
        this.priority = priority;

    }

    @Override
    public @NotNull ItemComponent build(@NotNull Function<Component, String> componentIdCreator, @NotNull Function<String, String> parser) {
        String id = componentIdCreator.apply(this);
        return Button.of(
                style,
                id,
                parser.apply(label),
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


    public static class Builder {

        private String name;

        private String label;

        private Emoji emoji;

        private ButtonStyle style;

        private int row;

        private int priority;

        private Builder(
                @Nullable String name,
                @Nullable String label,
                @Nullable Emoji emoji,
                @Nullable ButtonStyle style,
                int row,
                int priority
        ) {

            this.name = name;
            this.label = label;
            this.emoji = emoji;
            this.style = style;

            this.row = row;
            this.priority = priority;

        }

        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder setLabel(@NotNull String label) {
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


        public @NotNull ButtonTemplate build() {

            Objects.requireNonNull(label, "label == null");
            Objects.requireNonNull(style, "style == null");

            return new ButtonTemplate(name, label, emoji, style, row, priority);

        }

        public @NotNull Builder copy() {
            return new Builder(name, label, emoji, style, row, priority);
        }

    }

    public static @NotNull Builder builder() {
        return new Builder(null, null, null, null, 1, 1);
    }

    public static @NotNull ButtonTemplate create(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String name = typeMap.get("name", String.class);
        String label = typeMap.get("label", String.class);

        if (label == null) throw new InvalidComponentException("Button label is null!");

        String emojiRaw = typeMap.get("emoji", String.class);

        int row = typeMap.get("row", 1);
        int priority = typeMap.get("priority", 1);
        String styleRaw = typeMap.get("style", String.class);

        Emoji emoji = emojiRaw == null ? null : emoji(emojiRaw);
        ButtonStyle style = CommonUtils.getEnumValue(ButtonStyle.class, styleRaw);
        if (style == null) throw new InvalidComponentException("Invalid button style `" + styleRaw + "`");

        return builder()
                .setName(name)
                .setLabel(label)
                .setRow(row)
                .setPriority(priority)
                .setEmoji(emoji)
                .setStyle(style)
                .build();

    }

    private static @NotNull Emoji emoji(@NotNull String str) throws InvalidComponentException {

        if (!str.contains(":")) {

            try {
                return Emoji.fromFormatted(str);
            }

            catch (Throwable t) {
                throw new InvalidComponentException("Invalid emoji symbol `" + str + "`");
            }

        }

        String[] args = str.split(":");

        if (args.length != 3) throw new InvalidComponentException("Invalid emoji format `" + str + "`");

        String name = args[0];

        long id;
        try {
            id = Long.parseLong(args[1]);
        }

        catch (NumberFormatException e) {
            throw new InvalidComponentException("Invalid emoji id");
        }

        boolean animated = Boolean.parseBoolean(args[2]);

        return Emoji.fromCustom(name, id, animated);

    }

}
