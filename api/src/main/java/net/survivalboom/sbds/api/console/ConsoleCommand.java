package net.survivalboom.sbds.api.console;

import org.jetbrains.annotations.NotNull;

public interface ConsoleCommand {

    default void executes(@NotNull ConsoleExecutionInfo info) {}

}
