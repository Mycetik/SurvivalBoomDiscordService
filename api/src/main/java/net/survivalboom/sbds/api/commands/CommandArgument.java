package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandArgument {


    private final @NotNull String name;

    private final @Nullable String description;

    private final @Nullable String translationKey;

    private final int index;

    private final boolean required;


    private final @NotNull List<ArgumentScope> scopes = new ArrayList<>();

    private final @NotNull Argument<?> argument;


    public CommandArgument(
            @NotNull String name,
            @Nullable String description,
            @Nullable String translationKey,
            @Nullable Collection<ArgumentScope> scopes,
            @NotNull Argument<?> argument,
            int index,
            boolean required
    ) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(argument, "argument == null");

        this.name = name;

        this.description = description == null || description.isBlank() ? null : description;
        this.translationKey = translationKey == null || translationKey.isBlank() ? null: translationKey;

        this.argument = argument;

        if (scopes == null || scopes.isEmpty()) {
            this.scopes.addAll(List.of(ArgumentScope.SLASH, ArgumentScope.STRING, ArgumentScope.CONSOLE));
        }

        else {
            this.scopes.addAll(scopes);
        }

        this.index = index;
        this.required = required;

    }


    public boolean required() {
        return required;
    }

    public int index() {
        return index;
    }

    public @NotNull String name() {
        return name;
    }

    public @Nullable String description() {
        return description;
    }

    public @Nullable String translationKey() {
        return translationKey;
    }

    public @NotNull Argument<?> argument() {
        return argument;
    }

    public @NotNull List<ArgumentScope> scopes() {
        return scopes;
    }


    public boolean isSubCommand() {
        return argument instanceof SubCommandArgument;
    }

    //
    // BUILDER
    //

    public static @NotNull Builder create(@NotNull String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final String name;

        private @Nullable String description;

        private int index = 0;

        private boolean required = true;

        private @Nullable String translationKey;

        private Argument<?> argument;

        private final Set<ArgumentScope> scopes = new HashSet<>();


        public Builder(@NotNull String name) {
            this.name = name;
        }


        // NAME //

        public @NotNull String getName() {
            return name;
        }

        // DESCRIPTION //

        public @NotNull Builder setDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        public String getDescription() {
            return description;
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

        // TRANSLATION KEY //

        public @NotNull Builder setTranslationKey(@Nullable String key) {
            this.translationKey = translationKey;
            return this;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        // ARGUMENT //

        public @NotNull Builder setArgument(@NotNull Argument<?> argument) {
            this.argument = argument;
            return this;
        }

        public Argument<?> getArgument() {
            return argument;
        }

        // SCOPES //

        public @NotNull Builder setScopes(@Nullable Collection<ArgumentScope> scopes) {

            this.scopes.clear();

            if (scopes != null) {
                this.scopes.addAll(scopes);
            }

            return this;

        }

        public @NotNull Set<ArgumentScope> getScopes() {
            return scopes;
        }

        // BUILD //

        public @NotNull CommandArgument build() {
            return new CommandArgument(name, description, translationKey, scopes, argument, index, required);
        }

    }


}
