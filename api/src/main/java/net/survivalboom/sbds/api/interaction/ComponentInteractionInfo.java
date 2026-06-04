package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;

public class ComponentInteractionInfo<event extends GenericComponentInteractionCreateEvent> extends InteractionExecutionInfo<event> implements InteractionHolder {

    public ComponentInteractionInfo(
            @NotNull event event,
            @NotNull ISBDS sbds
    ) {
        super(event, sbds);
    }

}
