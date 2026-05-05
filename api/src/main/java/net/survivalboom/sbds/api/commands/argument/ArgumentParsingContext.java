package net.survivalboom.sbds.api.commands.argument;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.ICommandManager;
import org.jetbrains.annotations.NotNull;

public record ArgumentParsingContext(
        @NotNull ICommandManager.IRegisteredCommand<?, ?> rootCommand,
        @NotNull Command currentCommand,
        @NotNull CommandArgument currentArgument
) {

    public @NotNull ISBDS sbds() {
        return rootCommand.getManager().getSbds();
    }

}
