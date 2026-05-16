package net.survivalboom.sbds.api.commands.console;

import org.jetbrains.annotations.NotNull;

public interface ConsoleCommandExecutor {

    default void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {}

}
