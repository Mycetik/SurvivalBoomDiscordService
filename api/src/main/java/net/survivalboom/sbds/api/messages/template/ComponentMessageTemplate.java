package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.components.MessageInteractableComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ComponentMessageTemplate implements IMessageTemplate {

    private final String content;

    private final List<ComponentTemplate<? extends MessageTopLevelComponent>> components = new ArrayList<>();

    public ComponentMessageTemplate(
            @Nullable String content,
            @NotNull Collection<ComponentTemplate<? extends MessageTopLevelComponent>> collection
    ) {

        if (content != null && content.isBlank()) {
            throw new IllegalArgumentException("content is blank");
        }

        Objects.requireNonNull(collection, "collection == null");
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("components.size() == 0");
        }

        this.content = content;
        this.components.addAll(collection);

    }

    public @Nullable String getContent() {
        return content;
    }

    public @NotNull List<ComponentTemplate<? extends MessageTopLevelComponent>> getComponents() {
        return new ArrayList<>(this.components);
    }

    @Override
    public @NotNull MessageCreateBuilder createMessageData(
            @Nullable StringParser parser,
            @Nullable ComponentLinker linker
    ) {

        MessageCreateBuilder builder = new MessageCreateBuilder();

        builder.setContent(content);

        for (var template : components) {
            MessageTopLevelComponent component = template.build(parser, linker);
            builder.addComponents(component);
        }

        return builder;

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

    public static @NotNull Builder fromSection(@NotNull ConfigurationNode section) {

        String content = section.node("$content").getString();

        var builder = builder();
        builder.setContent(content);

        ConfigurationNode componentsSection = section.node("$components");
        for (ConfigurationNode node : componentsSection.childrenList()) {


        }

        // TODO Component API v2 все ще не реалызовано :(
        throw new RuntimeException("not implemented yet :(");

    }

    public static class Builder {

        private String content;

        private final List<ComponentTemplate<? extends MessageTopLevelComponent>> components = new ArrayList<>();

        private Builder() {}

        private Builder(@NotNull Builder builder) {
            this.components.addAll(builder.components);
        }

        private Builder(@NotNull ComponentMessageTemplate template) {
            this.components.addAll(template.components);
        }

        // COMPONENTS //

        public @NotNull Builder setComponents(@Nullable Collection<ComponentTemplate<? extends MessageTopLevelComponent>> components) {

            this.components.clear();

            if (components != null) {
                this.components.addAll(components);
            }

            return this;

        }

        public @NotNull Builder addComponents(@NotNull Collection<ComponentTemplate<? extends MessageTopLevelComponent>> components) {
            Objects.requireNonNull(components, "components == null");
            this.components.addAll(components);
            return this;
        }

        public @NotNull Builder addComponents(@NotNull ComponentTemplate<? extends MessageTopLevelComponent>... components) {
            return addComponents(List.of(components));
        }

        public @NotNull Builder addComponent(@NotNull ComponentTemplate<? extends MessageTopLevelComponent> template) {
            Objects.requireNonNull(template, "template == null");
            this.components.add(template);
            return this;
        }

        public @NotNull List<ComponentTemplate<? extends MessageTopLevelComponent>> getComponents() {
            return new ArrayList<>(this.components);
        }

        // CONTENT //

        public @NotNull Builder setContent(@Nullable String content) {
            this.content = content;
            return this;
        }

        public String getContent() {
            return content;
        }

        // build //

        public @NotNull ComponentMessageTemplate build() {
            return new ComponentMessageTemplate(content, components);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
