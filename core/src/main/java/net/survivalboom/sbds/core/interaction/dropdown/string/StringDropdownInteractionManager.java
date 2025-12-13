package net.survivalboom.sbds.core.interaction.dropdown.string;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.interaction.component.dropdown.string.IStringDropdownInteractionManager;
import net.survivalboom.sbds.api.interaction.component.dropdown.string.StringDropdownInteractionInfo;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.interaction.AbstractInteractionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringDropdownInteractionManager extends AbstractInteractionHandler<StringDropdownInteractionInfo, StringSelectInteractionEvent> implements IStringDropdownInteractionManager {

    public StringDropdownInteractionManager(@NotNull SBDS sbds) {
        super("StringDropdownManager", sbds);
    }

    @Override
    protected @Nullable String getIdFromEvent(StringSelectInteractionEvent event) {
        return event.getComponentId();
    }

    @Override
    protected @NotNull StringDropdownInteractionInfo createInteractionInfo(StringSelectInteractionEvent event) {
        return new StringDropdownInteractionInfo(event, sbds, logger);
    }

    @EventHandler
    public void onString(@NotNull StringSelectInteractionEvent event) {
        onEvent(event);
    }

}
