package net.survivalboom.sbds.api.commands.string;

import org.jetbrains.annotations.NotNull;

public interface StringCommandExecutor {

    default void executes(@NotNull StringExecutionInfo info) throws Throwable {}

}
