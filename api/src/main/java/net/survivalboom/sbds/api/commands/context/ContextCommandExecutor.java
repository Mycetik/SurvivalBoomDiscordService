package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface ContextCommandExecutor {

    void execute(@NotNull ContextInteractionInfo<?> info) throws Throwable;

    @NotNull Command.Type type();

    @NotNull ContextInteractionInfo<?> createInfo(@NotNull GenericContextInteractionEvent<?> event, @NotNull ISBDS sbds, @NotNull Logger logger);

}
