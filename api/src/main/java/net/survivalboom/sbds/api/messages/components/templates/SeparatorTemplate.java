package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.separator.Separator;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Objects;

@ConfigSerializable
public class SeparatorTemplate implements ComponentTemplate<Separator> {

    private int index = 0;

    private boolean divider = false;

    private Separator.Spacing type = Separator.Spacing.SMALL;


    public SeparatorTemplate(
            int index,
            boolean divider,
            @NotNull Separator.Spacing type
    ) {

        Objects.requireNonNull(type, "type == null");

        this.index = index;
        this.divider = divider;
        this.type = type;

    }

    @ApiStatus.Internal
    public SeparatorTemplate() {

    }

    @PostProcess
    private void validate() throws SerializationException {

        if (type == null) {
            throw new SerializationException("type == null");
        }

    }

    @Override
    public int getRow() {
        return index;
    }

    // COMPONENT //

    @Override
    public @NotNull Class<Separator> getComponentClass() {
        return Separator.class;
    }

    @Override
    public Component.@NotNull Type getType() {
        return Component.Type.SEPARATOR;
    }

    @Override
    public @NotNull Separator build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return Separator.create(divider, type);
    }

}
