package net.survivalboom.sbds.core.interaction.dropdown.entity;

import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.interaction.dropdown.entity.EntityDropdownInteractionInfo;
import net.survivalboom.sbds.api.interaction.dropdown.entity.IEntityDropdownInteractionManager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.interaction.AbstractInteractionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityDropdownInteractionManager extends AbstractInteractionHandler<EntityDropdownInteractionInfo, EntitySelectInteractionEvent> implements IEntityDropdownInteractionManager {

    public EntityDropdownInteractionManager(@NotNull SBDS sbds) {
        super("EntityDropdownManager", sbds);
    }

    @Override
    protected @Nullable String getIdFromEvent(EntitySelectInteractionEvent event) {
        return event.getComponentId();
    }

    @Override
    protected @NotNull EntityDropdownInteractionInfo createInteractionInfo(EntitySelectInteractionEvent event) {
        return new EntityDropdownInteractionInfo(event, sbds, logger);
    }


    @EventHandler
    public void onEntity(@NotNull EntitySelectInteractionEvent event) {
        onEvent(event);
    }

}
