package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface ContextCommandExecutor<E extends GenericContextInteractionEvent<?>> {

    void execute(@NotNull ContextInteractionInfo<E> info) throws Throwable;

}
