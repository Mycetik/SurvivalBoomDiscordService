package net.survivalboom.sbds.api.commands.argument;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public record ArgumentExecutionContext<R>(
        @NotNull CommandExecutionInfo<?, ?> executionInfo,
        @NotNull CommandArgument currentArgument,
        @NotNull R result,
        @NotNull BiConsumer<Command, ArgumentExecutionContext<R>> commandExecutor
) {
}
