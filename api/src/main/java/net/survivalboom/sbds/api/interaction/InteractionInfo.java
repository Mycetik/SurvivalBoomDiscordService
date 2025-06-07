package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.messages.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;

public abstract class InteractionInfo<E extends GenericInteractionCreateEvent> extends ExecutionInfo implements MessageReplyable, MessageEditable, ModalReplyable {

    protected final E event;

    protected final IReplyCallback replyCallback;

    protected final IMessageEditCallback editCallback;

    protected final IModalCallback modalCallback;

    public InteractionInfo(@NotNull E event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(sbds, logger);
        this.event = event;
        this.replyCallback = (IReplyCallback) event;
        this.editCallback = (IMessageEditCallback) event;
        this.modalCallback = (IModalCallback) event;
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

    public @NotNull E event() {
        return event;
    }

    @Override
    public @NotNull IReplyCallback replyCallback() {
        return replyCallback;
    }

    @Override
    public @NotNull IMessageEditCallback editCallback() {
        return editCallback;
    }

    @Override
    public @NotNull IModalCallback modalCallback() {
        return modalCallback;
    }

    protected @NotNull IMessage getMessage(@NotNull String key) {
        return Objects.requireNonNull(messages().getMessage(key, user(), true));
    }

}
