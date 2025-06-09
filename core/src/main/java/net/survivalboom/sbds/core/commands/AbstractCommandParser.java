package net.survivalboom.sbds.core.commands;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class AbstractCommandParser {

    protected final Command command;

    protected final Argument.ArgumentResources resources;

    protected TypeMap arguments;


    public AbstractCommandParser(@NotNull Command command, @NotNull Argument.ArgumentResources resources) {
        this.command = command;
        this.resources = resources;
    }

    public abstract void parse() throws ArgumentParseException;


    public boolean checkCount() {

        Objects.requireNonNull(arguments, "arguments == null");

        List<CommandArgument> requiredArguments = command.requiredArguments();

        return requiredArguments.stream().allMatch(a -> arguments.containsKey(a.name()));

    }


    public @NotNull TypeMap getArguments() {
        Objects.requireNonNull(arguments, "input wasn't parsed yet");
        return arguments;
    }

}
