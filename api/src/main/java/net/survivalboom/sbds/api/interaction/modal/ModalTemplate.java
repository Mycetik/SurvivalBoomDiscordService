package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ModalTemplate {

    private final String title;

    private final List<TextComponent> components;


    private ModalTemplate(@NotNull String title, @NotNull List<TextComponent> components) {
        this.title = title;
        this.components = components;
    }

    public @NotNull Modal create(@NotNull UUID uuid, @NotNull IMessages messages, @Nullable Placeholders placeholders) {

        Objects.requireNonNull(uuid, "uuid == null");
        Objects.requireNonNull(messages, "messages == null");

        String title = placeholders != null ? placeholders.parse(this.title) : this.title;
        Modal.Builder builder = Modal.create(uuid.toString(), title);

        for (TextComponent component : components) {
            String inputTitle = placeholders != null ? placeholders.parse(component.title()) : component.title();
            String inputPlaceholder = placeholders != null ? placeholders.parse(component.placeholder()) : component.placeholder();
            builder.addActionRow(TextInput.create(component.id(), inputTitle, component.style()).setPlaceholder(inputPlaceholder).build());
        }

        return builder.build();

    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<TextComponent> components = new ArrayList<>();

        private String title = "NO_TITLE";


        private Builder() {}


        public @NotNull Builder setTitle(@NotNull String title) {
            Objects.requireNonNull(title, "title == null");
            this.title = title;
            return this;
        }

        public @NotNull Builder addInput(@NotNull String id, @NotNull String title, @NotNull String placeholder, @NotNull TextInputStyle type) {
            components.add(new TextComponent(id, title, placeholder, type));
            return this;
        }

        public @NotNull ModalTemplate build() {
            return new ModalTemplate(title, components);
        }

    }


    private record TextComponent(@NotNull String id, @NotNull String title, @NotNull String placeholder, @NotNull TextInputStyle style) {}

}
