package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InteractionExecutionInfo<event extends GenericInteractionCreateEvent> extends ExecutionInfo implements IInteractionExecution<event> {

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

    @Override
    public Channel channel() {
        return event.getChannel();
    }

    @Override
    public @NotNull event interaction() {
        return event;
    }

    @Override
    public @NotNull RestAction<?> editRaw(@NotNull MessageEditData data) {
        return ((IMessageEditCallback) interaction()).editMessage(data);
    }

    @Override
    public @NotNull IModalCallback modalCallback0() {
        return (IModalCallback) event;
    }

    @Override
    public @NotNull IReplyCallback replyCallback0() {
        return (IReplyCallback) event;
    }

}
