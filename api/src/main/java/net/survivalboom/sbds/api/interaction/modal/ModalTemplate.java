package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
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

    private final List<TextComponent> components;

    private final String name;


    private ModalTemplate(@NotNull String title, @Nullable String name, @NotNull List<TextComponent> components) {
        this.title = title;
        this.name = name;
        this.components = components;
    }

    public @NotNull Modal create(@NotNull String id, @NotNull Function<String, String> parser) {

        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(parser, "parser == null");

        String title = parser.apply(this.title);
        Modal.Builder builder = Modal.create(id, title);

        for (TextComponent component : components) {
            String inputTitle = parser.apply(component.title);
            String inputPlaceholder = parser.apply(component.placeholder);
            builder.addActionRow(
                    TextInput.create(component.id(), inputTitle, component.style())
                            .setRequiredRange(component.min, component.max)
                            .setRequired(component.required)
                            .setPlaceholder(inputPlaceholder)
                            .build()
            );
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

        private final List<TextComponent> components;

        private String title;

        private String name;


        private Builder(@NotNull String title, @Nullable String name, @NotNull List<TextComponent> components) {
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
            components.add(new TextComponent(id, title, placeholder, type, min, max, required));
            return this;
        }

        public @NotNull ModalTemplate build() {
            return new ModalTemplate(title, name, new ArrayList<>(components));
        }

    }


    private record TextComponent(
            @NotNull String id,
            @NotNull String title,
            @NotNull String placeholder,
            @NotNull TextInputStyle style,
            int min,
            int max,
            boolean required
    ) {}

}
