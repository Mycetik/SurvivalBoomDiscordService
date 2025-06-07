package net.survivalboom.sbds.api.commands;

import net.dv8tion.jda.annotations.UnknownNullability;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.*;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class CommandExecutionInfo extends ExecutionInfo {

    protected final Command command;

    protected final String alias;

    protected final TypeMap arguments;


    public CommandExecutionInfo(@NotNull Command command, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {

        super(sbds, logger);

        this.command = command;
        this.alias = alias;
        this.arguments = arguments;

    }

    public @UnknownNullability IModule module() {
        return command.module();
    }

    public @NotNull Command command() {
        return command;
    }

    public @NotNull String alias() {
        return alias;
    }

    public @NotNull TypeMap arguments() {
        return arguments;
    }

}
