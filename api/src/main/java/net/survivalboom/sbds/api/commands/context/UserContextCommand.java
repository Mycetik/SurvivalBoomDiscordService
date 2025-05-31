package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface UserContextCommand extends ContextCommandExecutor {

    @Override
    default void execute(@NotNull ContextInteractionInfo<?> info) throws Throwable {
        execute((UserContextInteractionInfo) info);
    }

    void execute(@NotNull UserContextInteractionInfo info) throws Throwable;

    @Override
    default @NotNull Command.Type type() {
        return Command.Type.USER;
    }

    @Override
    default @NotNull ContextInteractionInfo<?> createInfo(@NotNull GenericContextInteractionEvent<?> event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        return new UserContextInteractionInfo((UserContextInteractionEvent) event, sbds, logger);
    }

}
