package net.survivalboom.sbds.api.commands.slash;

import net.survivalboom.sbds.api.commands.CommandExecutor;
import org.jetbrains.annotations.NotNull;

public interface SlashCommandExecutor extends CommandExecutor {

    default void executes(@NotNull SlashExecutionInfo info) throws Throwable {}

}
