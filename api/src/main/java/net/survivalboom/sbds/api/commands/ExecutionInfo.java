package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class ExecutionInfo {

    private final Command command;

    private final String alias;

    private final TypeMap arguments;


    private final Logger logger;

    private final ISBDS sbds;


    public ExecutionInfo(@NotNull Command command, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {

        this.command = command;
        this.alias = alias;
        this.arguments = arguments;

        this.logger = logger;
        this.sbds = sbds;

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

    public @NotNull Logger logger() {
        return logger;
    }

    public @NotNull ISBDS sbds() {
        return sbds;
    }

}
