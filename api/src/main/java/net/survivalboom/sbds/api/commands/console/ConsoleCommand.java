package net.survivalboom.sbds.api.commands.console;

import net.survivalboom.sbds.api.commands.CommandExecutor;
import org.jetbrains.annotations.NotNull;

public interface ConsoleCommand extends CommandExecutor<ConsoleExecutionInfo> {

    default void executes(@NotNull ConsoleExecutionInfo info) throws Exception {}

}
