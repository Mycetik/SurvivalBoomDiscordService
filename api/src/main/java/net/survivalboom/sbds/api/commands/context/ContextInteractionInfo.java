package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.InteractionInfoImpl;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class ContextInteractionInfo<E extends GenericContextInteractionEvent<?>> extends InteractionInfoImpl<E> {

    public ContextInteractionInfo(@NotNull E event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }


    @Override
    public @NotNull ModalActionBuilder replyModal(@NotNull String key) {
        return new ModalActionBuilder(sbds.getModalInteractionManager(), event, NamespacedKey.fromString(key));
    }

}
