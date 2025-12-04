package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ModalTemplate {

    private final String title;

    private final List<ModalComponent> components;

    private final String name;


    private ModalTemplate(@NotNull String title, @Nullable String name, @NotNull List<ModalComponent> components) {
        this.title = title;
        this.name = name;
        this.components = components;
    }

    public @NotNull Modal create(@NotNull String id, @NotNull Function<String, String> parser) {

        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(parser, "parser == null");

        Modal.Builder builder = Modal.create(id, parser.apply(title));

        for (ModalComponent component : components) {
            builder.addComponents(component.toComponent(parser));
        }

        return builder.build();

    }

    public @Nullable String name() {
        return name;
    }

    public static @NotNull Builder builder() {
        return new Builder("NO_TITLE", null, new ArrayList<>());
    }

    public static class Builder {

        private final List<ModalComponent> components;

        private String title;

        private String name;


        private Builder(@NotNull String title, @Nullable String name, @NotNull List<ModalComponent> components) {
            this.title = title;
            this.name = name;
            this.components = components;
        }


        public @NotNull Builder setTitle(@NotNull String title) {
            Objects.requireNonNull(title, "title == null");
            this.title = title;
            return this;
        }

        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder addInput(@NotNull String id, @NotNull String title, @NotNull String placeholder, @NotNull TextInputStyle type, int min, int max, boolean required) {
            components.add(ModalComponent.textInput(id, title, placeholder, type, min, max, required));
            return this;
        }

        public @NotNull Builder addAttachmentUpload(@NotNull String componentId, @NotNull String title) {
            components.add(ModalComponent.attachment(componentId, title));
            return this;
        }

        public @NotNull Builder addTextDisplay(@NotNull String markdown) {
            components.add(ModalComponent.textDisplay(markdown));
            return this;
        }

        public @NotNull Builder addEntitySelect(@NotNull String id,
                                                @NotNull String title,
                                                @NotNull EntitySelectMenu.SelectTarget target,
                                                int min,
                                                int max,
                                                @Nullable String placeholder) {
            components.add(ModalComponent.entitySelect(id, title, placeholder, target, min, max));
            return this;
        }

        public @NotNull ModalTemplate build() {
            return new ModalTemplate(title, name, new ArrayList<>(components));
        }

    }


    private TextInputStyle parseStyle(@NotNull String style) {
        return switch (style.toUpperCase()) {
            case "SHORT" -> TextInputStyle.SHORT;
            case "PARAGRAPH" -> TextInputStyle.PARAGRAPH;
            default -> TextInputStyle.SHORT;
        };
    }


    private record ModalComponent(
            @NotNull Type type,
            @Nullable String id,
            @Nullable String title,
            @Nullable String placeholder,
            @Nullable TextInputStyle textInputStyle,
            int min,
            int max,
            boolean required,
            @Nullable String content,
            @Nullable EntitySelectMenu.SelectTarget selectTarget
    ) {

        static ModalComponent textInput(String id, String title, String placeholder, TextInputStyle style, int min, int max, boolean required) {
            return new ModalComponent(Type.TEXT_INPUT, id, title, placeholder, style, min, max, required, null, null);
        }

        static ModalComponent attachment(String id, String title) {
            return new ModalComponent(Type.ATTACHMENT_UPLOAD, id, title, null, null, 0, 0, true, null, null);
        }

        static ModalComponent textDisplay(String content) {
            return new ModalComponent(Type.TEXT_DISPLAY, null, null, null, null, 0, 0, true, content, null);
        }

        static ModalComponent entitySelect(String id, String title, @Nullable String placeholder, EntitySelectMenu.SelectTarget target, int min, int max) {
            return new ModalComponent(Type.ENTITY_SELECT, id, title, placeholder, null, min, max, true, null, target);
        }

        net.dv8tion.jda.api.components.ModalTopLevelComponent toComponent(Function<String, String> parser) {
            return switch (type) {
                case TEXT_INPUT -> Label.of(
                           parser.apply(Objects.requireNonNull(title, "title")),
                            TextInput.create(
                                    Objects.requireNonNull(id, "component id"),
                                    Objects.requireNonNull(textInputStyle, "text input style")
                            )
                            .setPlaceholder(parser.apply(Objects.requireNonNull(placeholder, "placeholder")))
                            .setMinLength(min)
                            .setMaxLength(max)
                            .setRequired(required)
                            .build()
                    );
                case ATTACHMENT_UPLOAD -> Label.of(
                        parser.apply(Objects.requireNonNull(title, "title")),
                        AttachmentUpload.of(Objects.requireNonNull(id, "component id"))
                );
                case TEXT_DISPLAY -> TextDisplay.of(parser.apply(Objects.requireNonNull(content, "content")));
                case ENTITY_SELECT -> {
                    EntitySelectMenu.Builder builder = EntitySelectMenu.create(
                            Objects.requireNonNull(id, "component id"),
                            Objects.requireNonNull(selectTarget, "select target")
                    ).setMinValues(min).setMaxValues(max);
                    if (placeholder != null) {
                        builder.setPlaceholder(parser.apply(placeholder));
                    }
                    yield Label.of(
                            parser.apply(Objects.requireNonNull(title, "title")),
                            builder.build()
                    );
                }
            };
        }

        private enum Type {
            TEXT_INPUT,
            ATTACHMENT_UPLOAD,
            TEXT_DISPLAY,
            ENTITY_SELECT
        }

    }

}
