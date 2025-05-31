package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface MessageContextCommand extends ContextCommandExecutor {

    @Override
    default void execute(@NotNull ContextInteractionInfo<?> info) throws Throwable {
        execute((MessageContextInteractionInfo) info);
    }

    void execute(@NotNull MessageContextInteractionInfo info) throws Throwable;

    @Override
    default @NotNull Command.Type type() {
        return Command.Type.MESSAGE;
    }

    @Override
    default @NotNull ContextInteractionInfo<?> createInfo(@NotNull GenericContextInteractionEvent<?> event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        return new MessageContextInteractionInfo((MessageContextInteractionEvent) event, sbds, logger);
    }

}
