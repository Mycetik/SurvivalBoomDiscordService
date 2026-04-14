package net.survivalboom.sbds.api.messages.components;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ButtonTemplate implements ComponentTemplate {

    private final String name;

    private final String label;

    private final Emoji emoji;

    private final ButtonStyle style;

    private final int row;

    private final int priority;

    private final boolean isStatic;


    public ButtonTemplate(
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
    public int getRow() {
        return row;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public @NotNull Component.Type getType() {
        return Component.Type.BUTTON;
    }

    // BUILD //

    @Override
    public @NotNull Button build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return Button.of(
                style,
                ComponentLinker.stLink(linker, this),
                StringParser.stParseNullable(parser, label),
                emoji
        );
    }

    @Override
    public @NotNull TypeMap dump() {

        TypeMap map = new TypeMap().

        return ;

    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull Builder fromSection(@NotNull ConfigurationSection section) {

    }
    
    public static @NotNull Builder fromMap(@NotNull TypeMap map) throws InvalidComponentException {

        String name = map.get("name", String.class);
        boolean isStatic = Boolean.TRUE.equals(map.get("static", Boolean.class));

        String label = map.get("label", String.class);

        String emojiRaw = map.get("emoji", String.class);

        int row = map.get("row", 1);
        int priority = map.get("priority", 1);
        String styleRaw = map.get("style", String.class);

        Emoji emoji = emojiRaw == null ? null : Emoji.fromFormatted(emojiRaw);

        if (label == null && emoji == null) {
            throw new InvalidComponentException("Button label and button emoji are null!");
        }

        ButtonStyle style = CommonUtils.getEnumValue(ButtonStyle.class, styleRaw);
        if (style == null) {
            throw new InvalidComponentException("Invalid button style `" + styleRaw + "`");
        }

        return builder()
                .setName(name)
                .setLabel(label)
                .setRow(row)
                .setPriority(priority)
                .setStatic(isStatic)
                .setEmoji(emoji)
                .setStyle(style);
        
    }

    public static class Builder {

        private String name;

        private String label;

        private Emoji emoji;

        private ButtonStyle style;

        private int row;

        private int priority;

        private boolean isStatic;


        private Builder() {}

        private Builder(@NotNull Builder builder) {

            this.name = builder.name;
            this.label = builder.label;
            this.emoji = builder.emoji;
            this.style = builder.style;

            this.row = builder.row;
            this.priority = builder.priority;

            this.isStatic = builder.isStatic;

        }

        // NAME //
        
        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }
        
        public @Nullable String getName() {
            return name;
        }
        
        // LABEL //

        public @NotNull Builder setLabel(@Nullable String label) {
            this.label = label;
            return this;
        }
        
        public @Nullable String getLabel() {
            return label;
        }
        
        // EMOJI //

        public @NotNull Builder setEmoji(@Nullable Emoji emoji) {
            this.emoji = emoji;
            return this;
        }
        
        public @Nullable Emoji getEmoji() {
            return emoji;
        }
        
        // STYLE //

        public @NotNull Builder setStyle(@NotNull ButtonStyle style) {
            this.style = style;
            return this;
        }
        
        public @Nullable ButtonStyle getStyle() {
            return style;
        }
        
        // ROW //

        public @NotNull Builder setRow(int row) {
            if (row < 1 || row > 5) throw new IllegalArgumentException("Row must be between 1 and 5, got " + row);
            this.row = row;
            return this;
        }
        
        public int getRow() {
            return row;
        }
        
        // PRIORITY //

        public @NotNull Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public int getPriority() {
            return priority;
        }
        
        // STATIC //

        public @NotNull Builder setStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }
        
        public boolean isStatic() {
            return isStatic;
        }

        // BUILD //
        
        public @NotNull ButtonTemplate build() {
            return new ButtonTemplate(name, label, emoji, style, row, priority, isStatic);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
