package net.survivalboom.sbds.api.interaction.dropdown.string;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.survivalboom.sbds.api.interaction.button.ButtonTemplate;
import net.survivalboom.sbds.api.messages.Component;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StringDropdownTemplate implements Component {

    private final String name;

    private final int minCount;

    private final int maxCount;

    private final int row;

    private final int priority;

    private final boolean isStatic;

    private final String placeholder;

    private final List<Option> options;


    private StringDropdownTemplate(
            @Nullable String name,
            int minCount,
            int maxCount,
            int row,
            int priority,
            boolean isStatic,
            @Nullable String placeholder,
            @NotNull List<Option> options
    ) {
        this.name = name;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.row = row;
        this.priority = priority;
        this.isStatic = isStatic;
        this.placeholder = placeholder;
        this.options = options;
    }


    @Override
    public @NotNull ItemComponent build(@NotNull Function<Component, String> componentIdCreator, @NotNull Function<String, String> parser) {

        StringSelectMenu.Builder builder = StringSelectMenu.create(componentIdCreator.apply(this));
        for (Option option : options) {

            SelectOption selectOption = SelectOption.of(option.title, option.id)
                    .withDescription(option.description)
                    .withDefault(option.isDefault)
                    .withEmoji(option.emoji);

            builder.addOptions(selectOption);

        }

        return builder
                .setPlaceholder(placeholder)
                .setMaxValues(maxCount)
                .setMinValues(minCount)
                .build();

    }

    @Override
    public int row() {
        return row;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public @Nullable String name() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.Component.@NotNull Type type() {
        return net.dv8tion.jda.api.interactions.components.Component.Type.STRING_SELECT;
    }


    private record Option(@NotNull String id, @NotNull String title, @Nullable String description, @Nullable Emoji emoji, boolean isDefault) {}

    public static class Builder {

        private String name;

        private int row;

        private int priority;

        private int minCount;

        private int maxCount;

        private boolean isStatic;

        private String placeholder;

        private final List<Option> options;


        private Builder(
                @Nullable String name,
                int minCount,
                int maxCount,
                int row,
                int priority,
                boolean isStatic,
                @Nullable String placeholder,
                @NotNull List<Option> options
        ) {

            this.name = name;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.row = row;
            this.priority = priority;
            this.isStatic = isStatic;
            this.placeholder = placeholder;
            this.options = options;

        }

        private void addOption(@NotNull Option option) {
            options.add(option);
        }

        public @NotNull Builder addOption(@NotNull String id, @NotNull String title, @Nullable String description, @Nullable Emoji emoji, boolean isDefault) {
            options.add(new Option(id, title, description, emoji, isDefault));
            return this;
        }

        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder setMinCount(int v) {
            this.minCount = v;
            return this;
        }

        public @NotNull Builder setMaxCount(int v) {
            this.maxCount = v;
            return this;
        }

        public @NotNull Builder setRow(int row) {
            this.row = row;
            return this;
        }

        public @NotNull Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public @NotNull Builder setStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }

        public @NotNull Builder setPlaceholder(@Nullable String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public @NotNull StringDropdownTemplate build() {
            return new StringDropdownTemplate(name, minCount, maxCount, row, priority, isStatic, placeholder, new ArrayList<>(options));
        }

    }

    public static @NotNull Builder builder() {
        return new Builder(null, 0, 0, 0, 0, false, null, new ArrayList<>());
    }

    public static @NotNull StringDropdownTemplate create(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String name = typeMap.get("name", String.class);
        boolean isStatic = Boolean.TRUE.equals(typeMap.get("static", Boolean.class));

        int row = typeMap.get("row", 1);
        int priority = typeMap.get("priority", 1);

        int max = typeMap.getCastOrDefault("max", Integer.class, 1);
        int min = typeMap.getCastOrDefault("min", Integer.class, max);

        String placeholder = typeMap.get("placeholder", String.class);

        List<Option> options = createOptions(typeMap);

        Builder builder = builder()
                .setName(name)
                .setRow(row)
                .setPriority(priority)
                .setMinCount(min)
                .setMaxCount(max)
                .setPlaceholder(placeholder)
                .setStatic(isStatic);

        options.forEach(builder::addOption);

        return builder.build();

    }

    @SuppressWarnings("unchecked")
    private static @NotNull List<Option> createOptions(@NotNull TypeMap typeMap) throws InvalidComponentException {

        List<Option> options = new ArrayList<>();
        List<Map<String, Object>> mapList = typeMap.get("options", List.class);
        if (mapList == null) return options;

        for (Map<String, Object> map : mapList) {
            TypeMap optionMap = TypeMap.ofMap(map, true);
            Option option = createOption(optionMap);
            options.add(option);
        }

        return options;

    }

    private static @NotNull Option createOption(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String name = typeMap.get("name", String.class);
        if (name == null) throw new InvalidComponentException("Name of option is not defined");

        String label = typeMap.get("label", String.class);
        if (label == null) throw new InvalidComponentException("Label of option is not defined");

        String description = typeMap.get("description", String.class);

        String emojiRaw = typeMap.get("emoji", String.class);
        Emoji emoji = emojiRaw != null ? ButtonTemplate.emoji(emojiRaw) : null;

        boolean isDefault = Boolean.TRUE.equals(typeMap.getOrDefault("default", "false"));

        return new Option(name, label, description, emoji, isDefault);

    }

}
