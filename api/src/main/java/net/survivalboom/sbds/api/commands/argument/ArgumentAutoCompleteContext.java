package net.survivalboom.sbds.api.commands.argument;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;

public record ArgumentAutoCompleteContext(
        @NotNull ISlashCommandManager.IRegisteredSlashCommand rootCommand,
        @NotNull Command currentCommand,
        @NotNull CommandArgument currentArgument,
        @NotNull String input,
        @NotNull TypeMap arguments,
        @NotNull CommandAutoCompleteInteractionEvent event
) {

    public @NotNull ISBDS sbds() {
        return rootCommand.getManager().getSbds();
    }

}
