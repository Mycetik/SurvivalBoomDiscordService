package net.survivalboom.sbds.api.interaction.component.dropdown.string;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.survivalboom.sbds.api.interaction.component.dropdown.AbstractDropdownComponent;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class StringDropdownTemplate extends AbstractDropdownComponent<StringDropdownTemplate.Builder, StringDropdownTemplate, StringSelectMenu> {

    protected final List<Option> options = new ArrayList<>();

    public StringDropdownTemplate(
            @NotNull String name,
            @Nullable String title,
            @Nullable String placeholder,
            @NotNull Collection<Option> options,
            int minCount,
            int maxCount,
            int row,
            int priority,
            boolean isStatic
    ) {
        super(name, title, placeholder, minCount, maxCount, row, priority, isStatic, Component.Type.STRING_SELECT);
        Objects.requireNonNull(options, "option == null");
        this.options.addAll(options);
    }


    @Override
    public @NotNull Builder copy() {
        return new Builder(this);
    }

    @Override
    public @NotNull StringSelectMenu createComponent(@NotNull Function<String, String> parser, @Nullable Function<StringDropdownTemplate, String> componentIdCreator) {

        String id = componentIdCreator != null ? componentIdCreator.apply(this) : name;

        StringSelectMenu.Builder builder = StringSelectMenu.create(id);
        for (Option option : options) {

            SelectOption selectOption = SelectOption.of(option.title, option.id)
                    .withDescription(option.description)
                    .withDefault(option.isDefault)
                    .withEmoji(option.emoji);

            builder.addOptions(selectOption);

        }

        return builder
                .setPlaceholder(description)
                .setMaxValues(maxCount)
                .setMinValues(minCount)
                .build();

    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull Builder fromSection(@NotNull TypeMap map) throws InvalidComponentException {

        List<Option> options = createOptions(map);

        var builder = builder();
        AbstractDropdownComponent.fromSection(builder, map);
        builder.setOptions(options);

        return builder;

    }

    public static class Builder extends AbstractDropdownComponent.Builder<Builder, StringDropdownTemplate, StringSelectMenu> {

        private final List<Option> options = new ArrayList<>();


        protected Builder() {}

        protected Builder(@NotNull Builder builder) {
            super(builder);
            this.options.addAll(builder.options);
        }

        protected Builder(@NotNull StringDropdownTemplate template) {
            super(template);
            this.options.addAll(template.options);
        }

        // OPTIONS //

        private void addOption(@NotNull Option option) {
            options.add(option);
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

        //
        // BUILD
        //

        @Override
        public @NotNull StringDropdownTemplate build() {
            return new StringDropdownTemplate(name, title, description, options,  minCount, maxCount, row, priority, isStatic);
        }

        @Override
        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

    public record Option(@NotNull String id, @NotNull String title, @Nullable String description, @Nullable Emoji emoji, boolean isDefault) {}

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

    public static @NotNull Option createOption(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String name = typeMap.get("name", String.class);
        if (name == null) throw new InvalidComponentException("Name of option is not defined");

        String label = typeMap.get("label", String.class);
        if (label == null) throw new InvalidComponentException("Label of option is not defined");

        String description = typeMap.get("description", String.class);

        String emojiRaw = typeMap.get("emoji", String.class);
        Emoji emoji = emojiRaw != null ? Emoji.fromFormatted(emojiRaw) : null;

        boolean isDefault = Boolean.TRUE.equals(typeMap.getOrDefault("default", "false"));

        return new Option(name, label, description, emoji, isDefault);

    }

}
