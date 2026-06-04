package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.InteractionExecutionInfo;
import org.jetbrains.annotations.NotNull;

public abstract class ContextInteractionInfo<E extends GenericContextInteractionEvent<?>> extends InteractionExecutionInfo<E> {

    public ContextInteractionInfo(@NotNull E event, @NotNull ISBDS sbds) {
        super(event, sbds);
    }

}
