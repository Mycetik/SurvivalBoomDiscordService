package net.survivalboom.sbds.api.commands.console;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ConsoleExecutionInfo extends CommandExecutionInfo<IConsoleListener.IRegisteredConsoleCommand, IConsoleListener> {

    private final Logger logger;

    private final String input;

    public ConsoleExecutionInfo(
            @NotNull IConsoleListener.IRegisteredConsoleCommand rootCommand,
            @NotNull Command currentCommand,
            @NotNull String input,
            @NotNull String alias,
            @NotNull TypeMap arguments,
            @NotNull Logger logger
    ) {
        super(rootCommand, currentCommand, alias, arguments);
        this.input = input;
        this.logger = logger;
    }

    public @NotNull String input() {
        return input;
    }

    public @NotNull Logger logger() {
        return logger;
    }


}
