package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.commands.argument.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CommandArgument {

    private final @NotNull String name;

    private final @Nullable String description;

    private final int index;

    private final boolean required;

    private final @NotNull Argument<?> argument;


    public CommandArgument(@NotNull String name, @Nullable String description, @NotNull Argument<?> argument, int index, boolean required) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(argument, "argument == null");

        this.name = name;

        if (description == null || description.isBlank()) this.description = null;
        else this.description = description;

        this.argument = argument;
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

    public @NotNull Argument<?> argument() {
        return argument;
    }


    public static @NotNull CommandArgument create(@NotNull String name, @Nullable String description, @NotNull Argument<?> argument, int index, boolean required) {
        return new CommandArgument(name, description, argument, index, required);
    }

    public static @NotNull CommandArgument create(@NotNull String name, @Nullable String description, @NotNull Argument<?> argument) {
        return new CommandArgument(name, description, argument, 0, true);
    }

    public static @NotNull CommandArgument create(@NotNull String name, @NotNull Argument<?> argument) {
        return new CommandArgument(name, null, argument, 0, true);
    }

}
