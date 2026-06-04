package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.CanModal;
import net.survivalboom.sbds.api.interaction.CanReply;
import net.survivalboom.sbds.api.interaction.InteractionExecutionInfo;
import org.jetbrains.annotations.NotNull;

public abstract class ContextInteractionInfo<E extends GenericContextInteractionEvent<?>> extends InteractionExecutionInfo<E> implements CanReply, CanModal {

    public ContextInteractionInfo(@NotNull E event, @NotNull ISBDS sbds) {
        super(event, sbds);
    }

    @Override
    public @NotNull E interaction() {
        return event;
    }

}
