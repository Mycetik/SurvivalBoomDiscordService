package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.*;
import org.jetbrains.annotations.NotNull;

public class ModalInteractionInfo extends InteractionExecutionInfo<ModalInteractionEvent> {

    public ModalInteractionInfo(
            @NotNull ISBDS sbds,
            @NotNull ModalInteractionEvent event
    ) {
        super(event, true, sbds);
    }

}
