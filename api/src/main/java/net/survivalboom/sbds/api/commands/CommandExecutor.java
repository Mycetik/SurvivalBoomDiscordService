package net.survivalboom.sbds.api.commands;

import org.jetbrains.annotations.NotNull;

public interface CommandExecutor<T extends CommandExecutionInfo<?, ?>> {

    default void executes(@NotNull T info) throws Throwable {}

}
