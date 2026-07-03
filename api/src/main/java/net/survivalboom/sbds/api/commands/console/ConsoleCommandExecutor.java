package net.survivalboom.sbds.api.commands.console;

import net.survivalboom.sbds.api.commands.CommandExecutor;
import org.jetbrains.annotations.NotNull;

public interface ConsoleCommandExecutor extends CommandExecutor {

    default void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {}

}
