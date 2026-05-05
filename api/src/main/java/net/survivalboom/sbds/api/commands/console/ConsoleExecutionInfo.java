package net.survivalboom.sbds.api.commands.console;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;

public class ConsoleExecutionInfo extends CommandExecutionInfo<IConsoleListener.IRegisteredConsoleCommand, IConsoleListener> {

    private final String input;

    public ConsoleExecutionInfo(
            @NotNull IConsoleListener.IRegisteredConsoleCommand rootCommand,
            @NotNull Command currentCommand,
            @NotNull String input,
            @NotNull String alias,
            @NotNull TypeMap arguments
    ) {
        super(rootCommand, currentCommand, alias, arguments);
        this.input = input;
    }

    public @NotNull String input() {
        return input;
    }

}
