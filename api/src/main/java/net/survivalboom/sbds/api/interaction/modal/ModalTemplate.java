package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ModalTemplate {

    private final String name;

    private final String title;

    private final List<IModalComponent> components = new ArrayList<>();


    private ModalTemplate(@NotNull String title, @Nullable String name, @NotNull Collection<IModalComponent> components) {
        this.title = title;
        this.name = name;
        this.components.addAll(components);
    }

    public @NotNull Modal create(@NotNull String id, @NotNull Function<String, String> parser) {

        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(parser, "parser == null");

        Modal.Builder builder = Modal.create(id, parser.apply(title));

        for (var component : components) {
            builder.addComponents(component.createModalComponent(parser));
        }

        return builder.build();

    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public @NotNull Builder copy() {
        return new Builder(this);
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private String title;

        private final List<IModalComponent> components = new ArrayList<>();


        protected Builder() {}

        protected Builder(@NotNull ModalTemplate template) {
            this.name = template.name;
            this.title = template.title;
            this.components.addAll(template.components);
        }

        protected Builder(@NotNull Builder builder) {
            this.name = builder.name;
            this.title = builder.title;
            this.components.addAll(builder.components);
        }

        // NAME //

        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public @Nullable String getName() {
            return name;
        }

        // TITLE //

        public @NotNull Builder setTitle(@NotNull String title) {
            this.title = title;
            return this;
        }

        public @Nullable String getTitle() {
            return title;
        }

        // COMPONENTS //

        public @NotNull Builder addComponent(@NotNull IModalComponent component) {
            Objects.requireNonNull(component, "component == null");
            this.components.add(component);
            return this;
        }

        public @NotNull Builder addComponents(@NotNull Collection<IModalComponent> components) {
            Objects.requireNonNull(components, "components == null");
            this.components.addAll(components);
            return this;
        }

        public @NotNull Builder setComponents(@Nullable Collection<IModalComponent> components) {

            this.components.clear();

            if (components != null) {
                this.components.addAll(components);
            }

            return this;

        }

        //
        // BUILD
        //

        public @NotNull ModalTemplate build() {
            return new ModalTemplate(title, name, components);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
