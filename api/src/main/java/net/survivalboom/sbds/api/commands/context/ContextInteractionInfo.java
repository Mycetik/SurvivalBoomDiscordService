package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.interaction.InteractionExecutionInfo;
import org.jetbrains.annotations.NotNull;

public abstract class ContextInteractionInfo<E extends GenericContextInteractionEvent<?>> extends InteractionExecutionInfo<E> {

    protected final IContextCommandManager.IRegisteredContextCommand rootCommand;

    protected final Command currentCommand;

    protected final String alias;

    public ContextInteractionInfo(
            @NotNull E event,
            @NotNull IContextCommandManager.IRegisteredContextCommand rootCommand,
            @NotNull Command currentCommand,
            @NotNull String alias,
            @NotNull ISBDS sbds
    ) {
        super(event, currentCommand.isEphemeral(), sbds);
        this.rootCommand = rootCommand;
        this.currentCommand = currentCommand;
        this.alias = alias;
    }

    public @NotNull IContextCommandManager.IRegisteredContextCommand rootCommand() {
        return rootCommand;
    }

    public @NotNull Command currentCommand() {
        return currentCommand;
    }

    public @NotNull String alias() {
        return alias;
    }

}
