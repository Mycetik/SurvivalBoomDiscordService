package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class ComponentInteractionInfo<E extends GenericComponentInteractionCreateEvent> extends InteractionInfoImpl<E> implements HookEditable {

    public ComponentInteractionInfo(@NotNull E event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }

    @Override
    public @NotNull InteractionHook hook() {
        return event.getHook();
    }

}
