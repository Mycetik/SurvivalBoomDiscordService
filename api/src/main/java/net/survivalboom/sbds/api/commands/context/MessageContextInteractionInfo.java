package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class MessageContextInteractionInfo extends ContextInteractionInfo<MessageContextInteractionEvent> {

    public MessageContextInteractionInfo(@NotNull MessageContextInteractionEvent event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }

}
