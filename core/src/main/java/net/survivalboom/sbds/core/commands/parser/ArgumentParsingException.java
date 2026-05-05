package net.survivalboom.sbds.core.commands.parser;

import net.survivalboom.sbds.api.commands.CommandArgument;
import org.jetbrains.annotations.NotNull;

public class ArgumentParsingException extends Exception {

    private final CommandArgument argument;

    private final String input;

    public ArgumentParsingException(@NotNull CommandArgument argument, @NotNull String input, Exception e) {
        super(e);
        this.argument = argument;
        this.input = input;
    }

    public @NotNull CommandArgument getArgument() {
        return argument;
    }

    public @NotNull String getInput() {
        return input;
    }

}
