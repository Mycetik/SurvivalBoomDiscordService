package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.InteractionInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class ContextInteractionInfo<E extends GenericContextInteractionEvent<?>> extends InteractionInfo<E> {

    public ContextInteractionInfo(@NotNull E event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }

}
