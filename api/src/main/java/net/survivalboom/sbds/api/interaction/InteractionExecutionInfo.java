package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InteractionExecutionInfo<event extends GenericInteractionCreateEvent> extends ExecutionInfo {

    protected final event event;

    public InteractionExecutionInfo(
            @NotNull event event,
            @NotNull ISBDS sbds
    ) {
        super(sbds);
        this.event = event;
    }

    public @Nullable Member member() {
        return event.getMember();
    }

    public @NotNull User user() {
        return event.getUser();
    }

    public @Nullable Guild guild() {
        return event.getGuild();
    }

    public @NotNull event event() {
        return event;
    }

}
