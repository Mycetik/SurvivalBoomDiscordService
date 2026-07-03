package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class UserContextInteractionInfo extends ContextInteractionInfo<UserContextInteractionEvent> {

    public UserContextInteractionInfo(
            @NotNull UserContextInteractionEvent event,
            @NotNull IContextCommandManager.IRegisteredContextCommand rootCommand,
            @NotNull Command currentCommand,
            @NotNull String alias,
            @NotNull ISBDS sbds
    ) {
        super(event, rootCommand, currentCommand, alias, sbds);
    }

}
