package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.api.components.section.SectionContentComponent;
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
public class SectionTemplate implements ComponentTemplate<Section> {

    private int index = 0;

    private ComponentTemplate<? extends SectionAccessoryComponent> accessor;

    private List<ComponentTemplate<? extends SectionContentComponent>> children;


    public SectionTemplate(
            int index,
            @NotNull ComponentTemplate<? extends SectionAccessoryComponent> accessor,
            @NotNull Collection<ComponentTemplate<? extends SectionContentComponent>> children
    ) {

        Objects.requireNonNull(accessor, "accessor == null");
        Objects.requireNonNull(children, "children == null");

        this.index = index;
        this.accessor = accessor;
        this.children = new ArrayList<>(children);

    }

    @ApiStatus.Internal
    public SectionTemplate() {

    }

    @PostProcess
    private void validate() throws SerializationException {

        if (accessor == null) {
            throw new SerializationException("accessor == null");
        }

        if (children == null) {
            throw new SerializationException("children == null");
        }

        if (children.isEmpty()) {
            throw new SerializationException("children list is empty");
        }

    }

    @Override
    public int getRow() {
        return index;
    }


    public @NotNull ComponentTemplate<? extends SectionAccessoryComponent> getAccessor() {
        return accessor;
    }

    public @NotNull List<ComponentTemplate<? extends SectionContentComponent>> getChildren() {
        return new ArrayList<>(children);
    }

    // COMPONENT //

    @Override
    public @NotNull Class<Section> getComponentClass() {
        return Section.class;
    }

    @Override
    public Component.@NotNull Type getType() {
        return Component.Type.SECTION;
    }

    @Override
    public @NotNull Section build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {

        SectionAccessoryComponent accessory = this.accessor.build(parser, null);

        List<? extends SectionContentComponent> children = this.children.stream()
                .map(template -> template.build(parser, null))
                .toList();

        return Section.of(
                accessory,
                children
        );

    }

}
