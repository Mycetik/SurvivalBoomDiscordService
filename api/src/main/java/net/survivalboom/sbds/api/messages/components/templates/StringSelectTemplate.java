package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.survivalboom.sbds.api.messages.components.MessageInteractableComponentTemplate;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class StringSelectTemplate implements MessageInteractableComponentTemplate<StringSelectMenu> {

    private final String name;

    private final int minCount;

    private final int maxCount;

    private final int row;

    private final boolean isStatic;

    private final String placeholder;

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

        if (minCount < 1) {
            throw new IllegalArgumentException("min: " + minCount + " < 1");
        }

        if (maxCount < 1) {
            throw new IllegalArgumentException("max: " + maxCount + " < 1");
        }

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

    public record Option(
            @NotNull String id,
            @NotNull String title,
            @Nullable String description,
            @Nullable Emoji emoji,
            boolean isDefault
    ) {

        public Option {
            Objects.requireNonNull(id, "id == null");
            Objects.requireNonNull(title, "title == null");
        }

    }

    //
    // BUILDER
    //

    public static @NotNull Builder fromSection(@NotNull ConfigurationNode section) {

        String name = section.node("name").getString();

        int row = section.node("row").getInt();
        int min = section.node("min").getInt(1);
        int max = section.node("max").getInt(1);

        boolean isStatic = section.node("static").getBoolean();

        String placeholder = section.node("placeholder").getString();

        var builder = builder();

        ConfigurationNode optionsSection = section.node("options");
        for (ConfigurationNode node : optionsSection.childrenList()) {

            String id = node.node("id").getString();
            String title = node.node("title").getString();
            String description = node.node("description").getString();

            String emojiRaw = node.node("emoji").getString();
            Emoji emoji = emojiRaw != null ? Emoji.fromUnicode(emojiRaw) : null;

            boolean isDefault = node.node("default").getBoolean();

            builder.addOption(id, title, description, emoji, isDefault);

        }

        return builder
                .setName(name)
                .setRow(row)
                .setMinCount(min)
                .setMaxCount(max)
                .setStatic(isStatic)
                .setPlaceholder(placeholder);

    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private int row = 0;

        private int minCount = 1;

        private int maxCount = 1;

        private boolean isStatic = false;

        private String placeholder = null;

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

            Objects.requireNonNull(id, "id == null");
            Objects.requireNonNull(title, "title == null");

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
