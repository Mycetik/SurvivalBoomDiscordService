package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Command {

    // COMMAND BASE //

    private final String name;

    private final CommandExecutor executor;

    private final List<CommandArgument> arguments = new ArrayList<>();

    private final List<String> aliases = new ArrayList<>();

    // COMMAND INFO //

    private final @Nullable String description;

    private final @Nullable String translationKey;

    // PERMISSION //

    private final @Nullable Permission permission;


    public Command(
            @NotNull String name,
            @NotNull CommandExecutor executor,
            @Nullable Collection<CommandArgument> arguments,
            @Nullable Collection<String> aliases,

            @Nullable String description,
            @Nullable String translationKey,

            @Nullable Permission permission
    ) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(executor, "executor == null");

        this.name = name;
        this.executor = executor;

        this.description = description;
        this.translationKey = translationKey;

        this.permission = permission;

        if (arguments != null) {
            this.arguments.addAll(arguments);
        }

        if (aliases != null) {
            this.aliases.addAll(aliases);
        }

    }

    // COMMAND BASE //

    public @NotNull String getName() {
        return name;
    }

    public @NotNull CommandExecutor getExecutor() {
        return executor;
    }

    public @NotNull List<CommandArgument> getArguments() {
        return new ArrayList<>(arguments);
    }

    public @NotNull List<String> getAliases() {
        return new ArrayList<>(aliases);
    }

    // COMMAND INFO //

    public @Nullable String getDescription() {
        return description;
    }

    public @Nullable String getTranslationKey() {
        return translationKey;
    }

    // PERMISSION //

    public @Nullable Permission getPermission() {
        return permission;
    }

    // COPY //

    public @NotNull Builder copy() {
        return new Builder(this);
    }


    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        // COMMAND BASE //

        private String name;

        private CommandExecutor executor;

        private final List<CommandArgument> arguments = new ArrayList<>();

        private final List<String> aliases = new ArrayList<>();

        // COMMAND INFO //

        private @Nullable String description;

        private @Nullable String translationKey;

        // PERMISSION //

        private @Nullable Permission permission;


        private Builder() {}

        private Builder(Builder builder) {

            this.name = builder.name;
            this.executor = builder.executor;

            this.arguments.addAll(builder.arguments);
            this.aliases.addAll(builder.aliases);

            this.description = builder.description;
            this.translationKey = builder.translationKey;

            this.permission = builder.permission;

        }

        private Builder(Command command) {

            this.name = command.name;
            this.executor = command.executor;

            this.arguments.addAll(command.arguments);
            this.aliases.addAll(command.aliases);

            this.description = command.description;
            this.translationKey = command.translationKey;

            this.permission = command.permission;

        }

        // NAME //

        public @NotNull Builder setName(@NotNull String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        // EXECUTOR //

        public @NotNull Builder setExecutor(@NotNull CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        public CommandExecutor getExecutor() {
            return executor;
        }

        // ARGUMENTS //

        public @NotNull Builder setArguments(@Nullable Collection<CommandArgument> arguments) {

            this.arguments.clear();

            if (arguments != null) {
                this.arguments.addAll(arguments);
            }

            return this;

        }

        public @NotNull Builder addArgument(@NotNull CommandArgument argument) {
            Objects.requireNonNull(argument, "argument == null");
            this.arguments.add(argument);
            return this;
        }

        public @NotNull List<CommandArgument> getArguments() {
            return arguments;
        }

        // ALIASES //

        public @NotNull Builder setAliases(@Nullable Collection<String> aliases) {

            this.aliases.clear();

            if (aliases != null) {
                this.aliases.addAll(aliases);
            }

            return this;

        }

        public @NotNull Builder addAlias(@NotNull String alias) {

            Objects.requireNonNull(alias, "alias == null");
            this.aliases.add(alias);

            return this;

        }

        public @NotNull List<String> getAliases() {
            return aliases;
        }

        // DESCRIPTION //

        public @NotNull Builder setDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        public String getDescription() {
            return description;
        }

        // TRANSLATION KEY //

        public @NotNull Builder setTranslation(@Nullable String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        // PERMISSION //

        public @NotNull Builder setPermission(@Nullable Permission permission) {
            this.permission = permission;
            return this;
        }

        public Permission getPermission() {
            return permission;
        }

        // BUILD //

        public @NotNull Command build() {
            return new Command(name, executor, arguments, aliases, description, translationKey, permission);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
