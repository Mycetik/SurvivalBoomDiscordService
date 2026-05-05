package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.survivalboom.sbds.api.messages.components.MessageInteractableComponentTemplate;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Objects;

@ConfigSerializable
public class ButtonTemplate implements MessageInteractableComponentTemplate<Button> {

    private @Setting @Nullable String name;
    private @Setting @Nullable String url;

    private @Setting @Nullable String label;
    private @Setting @Nullable Emoji emoji;
    private @Setting @NotNull ButtonStyle style = ButtonStyle.SECONDARY;

    private @Setting int row = 1;
    private @Setting int index = 1;

    private @Setting("static") boolean isStatic = false;


    public ButtonTemplate(
            @NotNull String name,
            @Nullable String label,
            @Nullable Emoji emoji,
            @NotNull ButtonStyle style,
            int row,
            int index,
            boolean isStatic
    ) {

        Objects.requireNonNull(name, "name == null");

        this.name = name;
        this.url = null;

        this.label = label;
        this.emoji = emoji;
        this.style = style;
        this.row = row;
        this.index = index;
        this.isStatic = isStatic;

        if (label == null && emoji == null) {
            throw new IllegalArgumentException("Emoji and label == null");
        }

    }

    public ButtonTemplate(
            @NotNull String url,
            @Nullable String label,
            @Nullable Emoji emoji,
            @NotNull ButtonStyle style,
            int row,
            int index
    ) {

        Objects.requireNonNull(url, "url == null");

        this.name = null;
        this.url = url;

        this.label = label;
        this.emoji = emoji;
        this.style = style;
        this.row = row;
        this.index = index;

        this.isStatic = true;

    }

    @ApiStatus.Internal
    public ButtonTemplate() {}

    @PostProcess
    private void validate() throws SerializationException {

        if (name == null && url == null) {
            throw new SerializationException("url and name both are null; fuck you!");
        }

        if (label == null && emoji == null) {
            throw new SerializationException("Button label and emoji cannot both be null");
        }

    }

    @Override
    public int getRow() {
        return row;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    public @Nullable String getUrl() {
        return url;
    }

    // COMPONENT //

    @Override
    public @NotNull Class<Button> getComponentClass() {
        return Button.class;
    }

    @Override
    public @NotNull Component.Type getType() {
        return Component.Type.BUTTON;
    }

    @Override
    public @NotNull Button build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return Button.of(
                style,
                url == null ? ComponentLinker.stLink(linker, this) : url,
                StringParser.stParseNullable(parser, label),
                emoji
        );
    }

    public @NotNull Builder copy() {
        return new Builder(this);
    }

    @Override
    public String toString() {

        if (url != null) {
            return String.format(
                    "ButtonTemplate{url=%s, label=%s, emoji=%s, style=%s, index=%s, slot=%s, static=%s}",
                    url,
                    label,
                    emoji,
                    style,
                    row,
                    index,
                    isStatic
            );
        }

        return String.format(
                "ButtonTemplate{name=%s, label=%s, emoji=%s, style=%s, index=%s, slot=%s, static=%s}",
                name,
                label,
                emoji,
                style,
                row,
                index,
                isStatic
        );

    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private String url;


        private String label;

        private Emoji emoji;

        private ButtonStyle style;

        private int index;

        private int slot;

        private boolean isStatic;


        private Builder() {}

        private Builder(@NotNull Builder builder) {

            this.name = builder.name;
            this.url = builder.url;

            this.label = builder.label;
            this.emoji = builder.emoji;
            this.style = builder.style;

            this.index = builder.index;
            this.slot = builder.slot;

            this.isStatic = builder.isStatic;

        }

        private Builder(@NotNull ButtonTemplate template) {

            this.name = template.name;
            this.url = template.url;

            this.label = template.label;
            this.emoji = template.emoji;
            this.style = template.style;

            this.index = template.row;
            this.slot = template.index;

            this.isStatic = template.isStatic;

        }

        // NAME //
        
        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }
        
        public String getName() {
            return name;
        }

        // URL //

        public @NotNull Builder setUrl(@Nullable String url) {
            this.url = url;
            return this;
        }

        public String getUrl() {
            return url;
        }
        
        // LABEL //

        public @NotNull Builder setLabel(@Nullable String label) {
            this.label = label;
            return this;
        }
        
        public String getLabel() {
            return label;
        }
        
        // EMOJI //

        public @NotNull Builder setEmoji(@Nullable Emoji emoji) {
            this.emoji = emoji;
            return this;
        }
        
        public Emoji getEmoji() {
            return emoji;
        }
        
        // STYLE //

        public @NotNull Builder setStyle(@NotNull ButtonStyle style) {
            this.style = style;
            return this;
        }
        
        public ButtonStyle getStyle() {
            return style;
        }
        
        // SLOT //

        public @NotNull Builder setSlot(int slot) {

            if (slot < 1 || slot > 5) {
                throw new IllegalArgumentException("Slot must be between 1 and 5, got " + slot);
            }

            this.slot = slot;

            return this;

        }
        
        public int getSlot() {
            return slot;
        }
        
        // INDEX //

        public @NotNull Builder setIndex(int index) {
            this.index = index;
            return this;
        }
        
        public int getIndex() {
            return index;
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

            if (url != null) {
                return new ButtonTemplate(url, label, emoji, style, index, slot);
            }

            return new ButtonTemplate(name, label, emoji, style, index, slot, isStatic);

        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
