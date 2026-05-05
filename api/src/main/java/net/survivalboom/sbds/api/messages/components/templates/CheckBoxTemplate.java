package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroup;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CheckBoxTemplate implements ComponentTemplate<CheckboxGroup> {

    private String name = "null";

    private int index = 0;

    private boolean required = true;


    private List<Option> options;


    public CheckBoxTemplate(
            @NotNull String name,
            int index,
            boolean required,
            @NotNull Collection<Option> options
    ) {

        Objects.requireNonNull(name, "name == null");

        if (options.isEmpty()) {
            throw new IllegalArgumentException("options are empty");
        }

        this.name = name;
        this.index = index;
        this.required = required;

        this.options = new ArrayList<>(options);

    }

    @ApiStatus.Internal
    public CheckBoxTemplate() {

    }

    @PostProcess
    private void validate() throws SerializationException {

        if (name == null) {
            throw new SerializationException("name == null");
        }

        if (options.isEmpty()) {
            throw new SerializationException("options are empty");
        }

    }


    public boolean isRequired() {
        return required;
    }

    @Override
    public int getRow() {
        return index;
    }

    // COMPONENT //

    @Override
    public @NotNull Class<CheckboxGroup> getComponentClass() {
        return CheckboxGroup.class;
    }

    @Override
    public Component.@NotNull Type getType() {
        return Component.Type.RADIO_GROUP;
    }

    @Override
    public @NotNull CheckboxGroup build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {

        var builder = CheckboxGroup.create(name);

        for (var option : this.options) {

            String value = option.value;
            String label = option.label;
            String description = option.description;

            boolean isDefault = option.isDefault;

            builder.addOption(label, value, description, isDefault);

        }

        return builder.build();

    }

    @ConfigSerializable
    public record Option(@NotNull String value, @NotNull String label, @Nullable String description, @Setting("default") boolean isDefault) {

        public Option {

            Objects.requireNonNull(value, "value == null");
            Objects.requireNonNull(label, "label == null");

        }

    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private int index = 0;

        private boolean required = true;


        private final List<Option> options = new ArrayList<>();


        private Builder(Builder builder) {

            this.name = builder.name;
            this.index = builder.index;

            this.required = builder.required;

            this.options.addAll(builder.options);

        }

        private Builder(CheckBoxTemplate template) {

            this.name = template.name;
            this.index = template.index;

            this.required = template.required;

            this.options.addAll(template.options);

        }

        private Builder() {}

        // NAME //

        public @NotNull Builder setName(@NotNull String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        // INDEX //

        public @NotNull Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public int getIndex() {
            return index;
        }

        // REQUIRED //

        public @NotNull Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        // OPTIONS //

        public @NotNull Builder addOption(@NotNull String value, @NotNull String label, @Nullable String description, boolean isDefault) {
            this.options.add(new Option(value, label, description, isDefault));
            return this;
        }

        public @NotNull Builder addOptions(@Nullable Collection<Option> options) {

            this.options.clear();

            if (options != null) {
                this.options.addAll(options);
            }

            return this;

        }

        public @NotNull Builder addOptions(Option... options) {
            this.options.addAll(List.of(options));
            return this;
        }

        public @NotNull List<Option> getOptions() {
            return options;
        }

        // BUILD //

        public @NotNull CheckBoxTemplate build() {
            return new CheckBoxTemplate(name, index, required, options);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
