package net.survivalboom.sbds.core.interaction.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.interaction.component.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.interaction.component.button.IButtonInteractionManager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.interaction.AbstractInteractionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ButtonInteractionManager extends AbstractInteractionHandler<ButtonInteractionInfo, ButtonInteractionEvent> implements IButtonInteractionManager {

    public ButtonInteractionManager( @NotNull SBDS sbds) {
        super("ButtonInteractionManager", sbds);
    }

    @EventHandler
    public void onButtonClick(@NotNull ButtonInteractionEvent event) {

        onEvent(event);

    }


    @Override
    protected @Nullable String getIdFromEvent(ButtonInteractionEvent event) {
        return event.getButton().getCustomId();
    }

    @Override
    protected @NotNull ButtonInteractionInfo createInteractionInfo(ButtonInteractionEvent event) {
        return new ButtonInteractionInfo(event, sbds, logger);
    }

}
