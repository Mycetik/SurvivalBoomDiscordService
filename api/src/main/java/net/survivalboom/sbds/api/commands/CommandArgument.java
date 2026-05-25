package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandArgument {

    private final @NotNull String name;

    private final @Nullable String description;

    private final @Nullable String translationKey;

    private final int index;

    private final boolean required;


    private final @NotNull List<ArgumentScope> scopes;

    private final @NotNull Argument<?> argument;


    public CommandArgument(@NotNull String name, @Nullable String description, @Nullable String translationKey, @NotNull List<ArgumentScope> scopes, @NotNull Argument<?> argument, int index, boolean required) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(argument, "argument == null");

        this.name = name;

        this.description = description == null || description.isBlank() ? null : description;
        this.translationKey = translationKey == null || translationKey.isBlank() ? null: translationKey;

        this.argument = argument;
        this.scopes = new ArrayList<>(scopes);

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


    public static @NotNull CommandArgument create(@NotNull String name, @Nullable String description, @NotNull List<ArgumentScope> scopes, @NotNull Argument<?> argument, int index, boolean required) {
        return new CommandArgument(name, description, null, scopes, argument, index, required);
    }

    public static @NotNull CommandArgument create(@NotNull String name, @Nullable String description, @NotNull Argument<?> argument) {
        return new CommandArgument(name, description, null, defaultScopes(), argument, 0, true);
    }

    public static @NotNull CommandArgument create(@NotNull String name, @NotNull Argument<?> argument) {
        return new CommandArgument(name, null, null, defaultScopes(), argument, 0, true);
    }

    public static @NotNull List<ArgumentScope> defaultScopes() {
        return List.of(ArgumentScope.SLASH, ArgumentScope.STRING, ArgumentScope.CONSOLE);
    }

}
