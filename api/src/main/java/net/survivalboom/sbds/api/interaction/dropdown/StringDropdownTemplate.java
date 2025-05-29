package net.survivalboom.sbds.api.interaction.dropdown;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StringDropdownTemplate {

    private final List<Option> options;

    private StringDropdownTemplate(@NotNull List<Option> options) {
        this.options = options;
    }

    public @NotNull StringSelectMenu create(@NotNull UUID uuid, @NotNull IMessages messages, @Nullable Placeholders placeholders) {

        StringSelectMenu.Builder builder = StringSelectMenu.create(uuid.toString());
        for (Option option : options) {

            SelectOption selectOption = SelectOption.of(option.title, option.id)
                    .withDescription(option.description)
                    .withDefault(option.isDefault)
                    .withEmoji(option.emoji);

            builder.addOptions(selectOption);

        }

        return builder.build();

    }

    public static @NotNull Builder builder() {
        return new Builder();
    }


    private record Option(@NotNull String id, @NotNull String title, @Nullable String description, @Nullable Emoji emoji, boolean isDefault) {}

    public static class Builder {

        private final List<Option> options = new ArrayList<>();

        private Builder() {}

        public @NotNull Builder addOption(@NotNull String id, @NotNull String title, @Nullable String description, @Nullable Emoji emoji, boolean isDefault) {
            options.add(new Option(id, title, description, emoji, isDefault));
            return this;
        }

        public @NotNull Builder reset() {
            options.clear();
            return this;
        }

        public @NotNull StringDropdownTemplate build() {
            return new StringDropdownTemplate(new ArrayList<>(options));
        }

    }

}
