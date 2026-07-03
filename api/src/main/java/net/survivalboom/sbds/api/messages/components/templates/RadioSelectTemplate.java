package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.radiogroup.RadioGroup;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RadioSelectTemplate implements ComponentTemplate<RadioGroup> {

    private final String name;

    private final int index;

    private final boolean required;


    private final List<Option> options;


    public RadioSelectTemplate(
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

    @Override
    public int getRow() {
        return index;
    }

    // COMPONENT //

    @Override
    public @NotNull Class<RadioGroup> getComponentClass() {
        return RadioGroup.class;
    }

    @Override
    public Component.@NotNull Type getType() {
        return Component.Type.RADIO_GROUP;
    }

    @Override
    public @NotNull RadioGroup build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {

        var builder = RadioGroup.create(name);

        builder.setRequired(required);

        for (var option : this.options) {

            String name = StringParser.stParse(parser, option.name);
            String label = StringParser.stParse(parser, option.label);
            String description = StringParser.stParseNullable(parser, option.description);
            boolean isDefault = option.isDefault;

            builder.addOption(label, name, description, isDefault);

        }

        return builder.build();

    }

    @ConfigSerializable
    public record Option(@NotNull String name, @NotNull String label, @Nullable String description, @Setting("default") boolean isDefault) {

        public Option {

            Objects.requireNonNull(name, "name == null");
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

        private Builder(RadioSelectTemplate template) {

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

        public @NotNull RadioSelectTemplate build() {
            return new RadioSelectTemplate(name, index, required, options);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
