package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;

public class ComponentInteractionInfo<event extends GenericComponentInteractionCreateEvent> extends InteractionExecutionInfo<event> implements CanReply<event>, CanEdit<event>, CanModal<event> {

    public ComponentInteractionInfo(
            @NotNull event event,
            @NotNull ISBDS sbds
    ) {
        super(event, sbds);
    }

    @Override
    public @NotNull event interaction() {
        return event;
    }

}
