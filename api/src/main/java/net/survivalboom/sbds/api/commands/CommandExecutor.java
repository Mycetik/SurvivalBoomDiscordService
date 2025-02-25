package net.survivalboom.sbds.api.commands;

import org.jetbrains.annotations.NotNull;

public interface CommandExecutor {

    void execute(@NotNull ExecutionInfo info);

}
