package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.InteractionInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class UserContextInteractionInfo extends ContextInteractionInfo<UserContextInteractionEvent> {

    public UserContextInteractionInfo(@NotNull UserContextInteractionEvent event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }

}
