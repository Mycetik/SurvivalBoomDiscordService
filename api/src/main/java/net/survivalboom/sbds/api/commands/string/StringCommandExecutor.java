package net.survivalboom.sbds.api.commands.string;

import net.survivalboom.sbds.api.commands.CommandExecutor;
import org.jetbrains.annotations.NotNull;

public interface StringCommandExecutor extends CommandExecutor {

    default void executes(@NotNull StringExecutionInfo info) throws Throwable {}

}
