package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.CanEdit;
import net.survivalboom.sbds.api.interaction.CanModal;
import net.survivalboom.sbds.api.interaction.CanReply;
import net.survivalboom.sbds.api.interaction.InteractionExecutionInfo;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public abstract class ContextInteractionInfo<E extends GenericContextInteractionEvent<?>> extends InteractionExecutionInfo<E> implements CanReply<E>, CanModal<E> {

    public ContextInteractionInfo(@NotNull E event, @NotNull ISBDS sbds) {
        super(event, sbds);
    }

    @Override
    public @NotNull E interaction() {
        return event;
    }

}
