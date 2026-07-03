package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.interaction.*;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;

public abstract class CommandExecutionInfo<cmd extends ICommandManager.IRegisteredCommand<cmd, manager>, manager extends ICommandManager<cmd, manager>> extends ExecutionInfo {

    protected final cmd rootCommand;

    protected final Command currentCommand;

    protected final String alias;

    protected final TypeMap arguments;


    public CommandExecutionInfo(
            @NotNull cmd rootCommand,
            @NotNull Command currentCommand,
            @NotNull String alias,
            @NotNull TypeMap arguments
    ) {

        super(rootCommand.getManager().getSbds());

        this.rootCommand = rootCommand;
        this.currentCommand = currentCommand;

        this.alias = alias;
        this.arguments = arguments;

    }

    // CMD //

    public @NotNull cmd rootCommand() {
        return rootCommand;
    }

    public @NotNull Command currentCommand() {
        return currentCommand;
    }

    // ALIAS //

    public @NotNull String alias() {
        return alias;
    }

    // ARGS //

    public @NotNull TypeMap arguments() {
        return arguments;
    }

}
