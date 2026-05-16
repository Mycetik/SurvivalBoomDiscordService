package net.survivalboom.sbds.api.commands.slash;

import org.jetbrains.annotations.NotNull;

public interface SlashCommandExecutor {

    default void executes(@NotNull SlashExecutionInfo info) throws Throwable {}

}
