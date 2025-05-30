package net.survivalboom.sbds.api.interaction.dropdown.entity;

import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.ComponentInteractionInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class EntityDropdownInteractionInfo extends ComponentInteractionInfo<EntitySelectInteractionEvent> {

    public EntityDropdownInteractionInfo(@NotNull EntitySelectInteractionEvent event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }


    public @NotNull Mentions mentions() {
        return event.getMentions();
    }



}
