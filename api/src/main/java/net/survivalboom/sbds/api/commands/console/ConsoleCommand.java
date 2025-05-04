package net.survivalboom.sbds.api.commands.console;

import org.jetbrains.annotations.NotNull;

public interface ConsoleCommand {

    default void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {}

}
