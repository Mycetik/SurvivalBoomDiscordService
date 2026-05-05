package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.survivalboom.sbds.api.messages.components.MessageInteractableComponentTemplate;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@ConfigSerializable
public class StringSelectTemplate implements MessageInteractableComponentTemplate<StringSelectMenu> {

    private String name;

    @Setting("min")
    private int minCount = 1;

    @Setting("max")
    private int maxCount = 1;

    private int row = 1;

    @Setting("static")
    private boolean isStatic = false;

    private String placeholder;

    private final List<Option> options = new ArrayList<>();


    public StringSelectTemplate(
            @Nullable String name,
            int minCount,
            int maxCount,
            int row,
            boolean isStatic,
            @Nullable String placeholder,
            @Nullable Collection<Option> options
    ) {

        this.name = name;
        this.placeholder = placeholder;

        this.minCount = minCount;
        this.maxCount = maxCount;

        this.row = row;

        if (name != null) {
            this.isStatic = isStatic;
        }

        else {
            this.isStatic = true;
        }

        if (options != null) {
            this.options.addAll(options);
        }

    }

    @ApiStatus.Internal
    public StringSelectTemplate() {

    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public int getRow() {
        return row;
    }

    // COMPONENT //

    @Override
    public @NotNull Class<StringSelectMenu> getComponentClass() {
        return StringSelectMenu.class;
    }

    @Override
    public @NotNull Component.Type getType() {
        return Component.Type.STRING_SELECT;
    }

    @Override
    public @NotNull StringSelectMenu build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {

        StringSelectMenu.Builder builder = StringSelectMenu.create(ComponentLinker.stLink(linker, this));
        for (Option option : options) {

            String title = StringParser.stParse(parser, option.title);
            String description = StringParser.stParseNullable(parser, option.description);

            SelectOption selectOption = SelectOption.of(title, option.id)
                    .withDescription(description)
                    .withDefault(option.isDefault)
                    .withEmoji(option.emoji);

            builder.addOptions(selectOption);

        }

        return builder
                .setPlaceholder(StringParser.stParseNullable(parser, placeholder))
                .setMaxValues(maxCount)
                .setMinValues(minCount)
                .build();

    }

    public @NotNull Builder copy() {
        return new Builder(this);
    }

    //
    // OPTION
    //

    @ConfigSerializable
    public record Option(@NotNull String id, @NotNull String title, @Nullable String description, @Nullable Emoji emoji, @Setting("default") boolean isDefault) {

        public Option {
            Objects.requireNonNull(id, "id == null");
            Objects.requireNonNull(title, "title == null");
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

        private int row;

        private int minCount;

        private int maxCount;

        private boolean isStatic;

        private String placeholder;

        private final List<Option> options = new ArrayList<>();


        private Builder(Builder builder) {

            this.name = builder.name;
            this.minCount = builder.minCount;
            this.maxCount = builder.maxCount;
            this.row = builder.row;
            this.isStatic = builder.isStatic;
            this.placeholder = builder.placeholder;

            this.options.addAll(builder.options);

        }

        private Builder(StringSelectTemplate template) {

            this.name = template.name;
            this.minCount = template.minCount;
            this.maxCount = template.maxCount;
            this.row = template.row;
            this.isStatic = template.isStatic;
            this.placeholder = template.placeholder;

            this.options.addAll(template.options);

        }

        private Builder() {}

        // NAME //

        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        // min //

        public @NotNull Builder setMinCount(int v) {
            this.minCount = v;
            return this;
        }

        public int getMinCount() {
            return minCount;
        }

        // max //

        public @NotNull Builder setMaxCount(int v) {
            this.maxCount = v;
            return this;
        }

        public int getMaxCount() {
            return maxCount;
        }

        // row //

        public @NotNull Builder setRow(int row) {
            this.row = row;
            return this;
        }

        public int getRow() {
            return row;
        }

        // static //

        public @NotNull Builder setStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }

        public boolean isStatic() {
            return isStatic;
        }

        // placeholder //

        public @NotNull Builder setPlaceholder(@Nullable String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        // OPTIONS //

        public @NotNull Builder addOption(@NotNull Option option) {
            options.add(option);
            return this;
        }

        public @NotNull Builder addOption(
                @NotNull String id,
                @NotNull String title,
                @Nullable String description,
                @Nullable Emoji emoji,
                boolean isDefault
        ) {
            options.add(new Option(id, title, description, emoji, isDefault));
            return this;
        }

        public @NotNull Builder addOptions(@NotNull Collection<Option> options) {
            this.options.addAll(options);
            return this;
        }

        public @NotNull Builder setOptions(@Nullable Collection<Option> options) {

            this.options.clear();

            if (options != null) {
                this.options.addAll(options);
            }

            return this;

        }

        public @NotNull List<Option> getOptions() {
            return options;
        }

        // BUILD //

        public @NotNull StringSelectTemplate build() {
            return new StringSelectTemplate(name, minCount, maxCount, row, isStatic, placeholder, new ArrayList<>(options));
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
