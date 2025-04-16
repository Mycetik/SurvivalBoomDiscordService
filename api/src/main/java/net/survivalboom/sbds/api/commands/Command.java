package net.survivalboom.sbds.api.commands;

import net.dv8tion.jda.annotations.UnknownNullability;
import net.survivalboom.sbds.api.commands.argument.internal.SubCommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ToIntFunction;

public class Command {

    private final String name;

    private final IModule module;


    private final List<CommandArgument> arguments = new ArrayList<>();

    private final List<Command> subcommands = new ArrayList<>();


    private final List<String> aliases = new ArrayList<>();

    private String description;

    private String usage;


    private CommandExecutor executor;


    public Command(@NotNull String name, @Nullable IModule module) {
        Objects.requireNonNull(name, "name == null");
        this.name = name;
        this.module = module;
    }


    public @NotNull Command withDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    public @NotNull Command withUsage(@Nullable String usage) {
        this.usage = usage;
        return this;
    }


    public @NotNull Command withArguments(@NotNull CommandArgument... arguments) {

        this.arguments.addAll(Arrays.asList(arguments));

        sortArguments();

        return this;

    }

    public @NotNull Command withAliases(@NotNull String... aliases) {
        Objects.requireNonNull(aliases, "aliases == null");
        this.aliases.addAll(List.of(aliases));
        return this;
    }

    public @NotNull Command withAliases(@NotNull List<String> aliases) {
        Objects.requireNonNull(aliases, "aliases == null");
        this.aliases.addAll(aliases);
        return this;
    }






    public @NotNull Command withArguments(@NotNull Iterable<CommandArgument> arguments) {

        if (hasSubcommands()) throw new IllegalStateException("Couldn't add argument to command with subcommands");

        Objects.requireNonNull(arguments, "arguments == null");
        for (CommandArgument argument : arguments) {
            this.arguments.add(argument);
        }

        sortArguments();

        return this;

    }

    public @NotNull Command withArgument(@NotNull CommandArgument argument) {

        if (hasSubcommands()) throw new IllegalStateException("Couldn't add argument to command with subcommands");

        Objects.requireNonNull(argument, "argument == null");

        arguments.add(argument);
        sortArguments();

        return this;

    }

    private void sortArguments() {

        ToIntFunction<CommandArgument> function = argument -> {

            int price = argument.index();
            if (!argument.required()) price += 10;

            return price;

        };

        Comparator<CommandArgument> comparator = Comparator.comparingInt(function);

        arguments.sort(comparator);

    }





    public @NotNull Command executes(@NotNull CommandExecutor executor) {
        Objects.requireNonNull(executor, "executor == null");
        this.executor = executor;
        return this;
    }


    public @NotNull Command withSubcommand(@NotNull Command command) {

        Objects.requireNonNull(command, "command == null");

        subcommands.add(command);

        arguments.clear();
        arguments.add(CommandArgument.create("subcommand", new SubCommandArgument(subcommands)));

        return command;

    }

    public @NotNull Command withSubcommand(@NotNull CommandBase base, @Nullable IModule module) {
        return withSubcommand(base.build(module));
    }







    public @NotNull String getName() {
        return name;
    }

    public @UnknownNullability IModule module() {
        return module;
    }

    public @NotNull List<String> aliases() {
        return new ArrayList<>(aliases);
    }

    public @NotNull CommandExecutor executor() {
        return executor;
    }

    public @NotNull List<Command> subcommands() {
        return subcommands;
    }

    public @NotNull List<CommandArgument> arguments() {
        return new ArrayList<>(arguments);
    }

    public @NotNull List<CommandArgument> requiredArguments() {
        return arguments().stream().filter(CommandArgument::required).toList();
    }

    public @NotNull List<CommandArgument> optionalArguments() {
        return arguments().stream().filter(a -> !a.required()).toList();
    }

    public @Nullable String usage() {
        return usage;
    }

    public @Nullable String description() {
        return description;
    }

    public boolean hasSubcommands() {
        return !subcommands.isEmpty();
    }

}
