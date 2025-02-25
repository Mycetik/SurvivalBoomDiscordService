package net.survivalboom.sbds.api.commands.string;

import org.jetbrains.annotations.NotNull;

public interface StringCommand {

    default void executes(@NotNull StringExecutionInfo info) {}

}
