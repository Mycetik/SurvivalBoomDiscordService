package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@ConfigSerializable
public class ComponentMessageTemplate implements IMessageTemplate {

    private final List<ComponentTemplate<? extends MessageTopLevelComponent>> components = new ArrayList<>();

    public ComponentMessageTemplate(
            @NotNull Collection<ComponentTemplate<? extends MessageTopLevelComponent>> collection
    ) {

        Objects.requireNonNull(collection, "collection == null");
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("components.size() == 0");
        }

        this.components.addAll(collection);
    }

    @ApiStatus.Internal
    public ComponentMessageTemplate() {

    }

    @PostProcess
    private void validate() throws SerializationException {

        if (this.components.isEmpty()) {
            throw new SerializationException("components.size() == 0");
        }

    }


    public @NotNull List<ComponentTemplate<? extends MessageTopLevelComponent>> getComponents() {
        return new ArrayList<>(this.components);
    }


    @Override
    public @NotNull MessageCreateData createMessageData(
            @Nullable StringParser parser,
            @Nullable ComponentLinker linker
    ) {

        MessageCreateBuilder builder = new MessageCreateBuilder();

        for (var template : components) {
            MessageTopLevelComponent component = template.build(parser, linker);
            builder.addComponents(component);
        }

        return builder.build();

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

        private final List<ComponentTemplate<? extends MessageTopLevelComponent>> components = new ArrayList<>();

        private Builder() {}

        private Builder(@NotNull Builder builder) {
            this.components.addAll(builder.components);
        }

        private Builder(@NotNull ComponentMessageTemplate template) {
            this.components.addAll(template.components);
        }

        // build //

        public @NotNull ComponentMessageTemplate build() {
            return new ComponentMessageTemplate(components);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
