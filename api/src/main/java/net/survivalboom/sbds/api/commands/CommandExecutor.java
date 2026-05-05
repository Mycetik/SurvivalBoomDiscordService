package net.survivalboom.sbds.api.commands;

import org.jetbrains.annotations.NotNull;

public interface CommandExecutor<T extends CommandExecutionInfo<?, ?>> {

    void execute(@NotNull T info) throws Exception;

}
