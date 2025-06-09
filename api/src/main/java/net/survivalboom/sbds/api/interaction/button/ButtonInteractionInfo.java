package net.survivalboom.sbds.api.interaction.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.ComponentInteractionInfo;
import net.survivalboom.sbds.api.interaction.HookEditable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ButtonInteractionInfo extends ComponentInteractionInfo<ButtonInteractionEvent> {


    public ButtonInteractionInfo(@NotNull ButtonInteractionEvent event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }

}
