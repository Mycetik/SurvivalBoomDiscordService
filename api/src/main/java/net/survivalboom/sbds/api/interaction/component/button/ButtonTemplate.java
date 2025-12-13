package net.survivalboom.sbds.api.interaction.component.button;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.survivalboom.sbds.api.interaction.component.AbstractInteractionComponent;
import net.survivalboom.sbds.api.interaction.component.IComponent;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ButtonTemplate extends AbstractInteractionComponent<ButtonTemplate.Builder, ButtonTemplate, Button> {

    private final String label;

    private final Emoji emoji;

    private final ButtonStyle style;


    public ButtonTemplate(
            @NotNull String name,
            @Nullable String label,
            @Nullable Emoji emoji,
            @NotNull ButtonStyle style,
            int row,
            int priority,
            boolean isStatic
    ) {

        super(name, row, priority, isStatic, Component.Type.BUTTON);

        this.label = label;
        this.emoji = emoji;
        this.style = style;

        if (label == null && emoji == null) {
            throw new IllegalArgumentException("Emoji and label == null");
        }

    }

    public @Nullable String getLabel() {
        return label;
    }

    public @Nullable Emoji getEmoji() {
        return emoji;
    }

    public @NotNull ButtonStyle getStyle() {
        return style;
    }


    @Override
    public @NotNull Button createComponent(
            @NotNull Function<String, String> parser,
            @Nullable Function<IComponent, String> componentIdCreator
    ) {
        String id = componentIdCreator != null ? componentIdCreator.apply(this) : name;
        return Button.of(style, id, label != null ? parser.apply(label) : null, emoji);
    }

    @Override
    public @NotNull ButtonTemplate.Builder copy() {
        return new Builder(this);
    }

    //
    // BUILDER
    //

    public static @NotNull Builder fromSection(@NotNull TypeMap map) throws InvalidComponentException {

        String label = map.get("label", String.class);
        String emojiRaw = map.get("emoji", String.class);
        Emoji emoji = emojiRaw == null ? null : Emoji.fromFormatted(emojiRaw);

        if (label == null && emoji == null) {
            throw new InvalidComponentException("Button label and button emoji are null!");
        }

        String styleRaw = map.get("style", String.class);
        ButtonStyle style = CommonUtils.getEnumValue(ButtonStyle.class, styleRaw);
        if (style == null) {
            throw new InvalidComponentException("Invalid button style `" + styleRaw + "`");
        }

        var builder = builder();

        AbstractInteractionComponent.fromSection(builder, map);

        return builder
                .setLabel(label)
                .setEmoji(emoji)
                .setStyle(style);

    }

    public static class Builder extends AbstractInteractionComponent.Builder<Builder, ButtonTemplate, Button> {

        private String label;

        private Emoji emoji;

        private ButtonStyle style;


        private Builder() {}

        private Builder(@NotNull Builder builder) {

            super(builder);

            this.label = builder.label;
            this.emoji = builder.emoji;
            this.style = builder.style;

        }

        private Builder(@NotNull ButtonTemplate template) {

            super(template);

            this.label = template.label;
            this.emoji = template.emoji;
            this.style = template.style;

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

        // BUILDER //

        @Override
        public @NotNull ButtonTemplate build() {
            return new ButtonTemplate(name, label, emoji, style, row, priority, isStatic);
        }

        @Override
        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

}
