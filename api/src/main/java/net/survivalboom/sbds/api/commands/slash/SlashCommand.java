package net.survivalboom.sbds.api.commands.slash;

import org.jetbrains.annotations.NotNull;

public interface SlashCommand {

    default void executes(@NotNull SlashExecutionInfo info) throws Throwable {}

}
