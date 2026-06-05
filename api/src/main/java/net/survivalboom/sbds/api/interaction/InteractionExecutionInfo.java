package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InteractionExecutionInfo<event extends GenericInteractionCreateEvent> extends ExecutionInfo implements InteractionHolder {

    protected final event event;

    public InteractionExecutionInfo(
            @NotNull event event,
            @NotNull ISBDS sbds
    ) {
        super(sbds);
        this.event = event;
    }

    public @NotNull event event() {
        return event;
    }

    @Override
    public @NotNull Object source() {
        return event;
    }


    @Override
    public @Nullable Member member() {
        return event.getMember();
    }

    @Override
    public @NotNull User user() {
        return event.getUser();
    }

    @Override
    public @Nullable Guild guild() {
        return event.getGuild();
    }

    @Override
    public Channel channel() {
        return event.getChannel();
    }

    // EDIT //

    @Override
    public @NotNull RestAction<?> edit(@NotNull MessageEditData data) {

        if (!(event instanceof IDeferrableCallback callback)) {
            throw new IllegalStateException("No edit method applicable to `" + this + "`");
        }

        return callback.getHook().editOriginal(data);

    }

    @Override
    public @NotNull RestAction<?> editRaw(@NotNull String txt) {

        if (!(event instanceof IDeferrableCallback callback)) {
            throw new IllegalStateException("No edit method applicable to `" + this + "`");
        }

        return callback.getHook().editOriginal(txt);

    }

    // SEND ONLY //

    @Override
    public @NotNull RestAction<?> send(@NotNull String txt, boolean ephemeral) {

        if (!(event instanceof IReplyCallback callback)) {
            throw new IllegalStateException("No reply method applicable to `" + this + "`");
        }

        return callback.reply(txt).setEphemeral(ephemeral);

    }

    @Override
    public @NotNull RestAction<?> send(@NotNull MessageCreateData data, boolean ephemeral) {

        if (!(event instanceof IReplyCallback callback)) {
            throw new IllegalStateException("No reply method applicable to `" + this + "`");
        }

        return callback.reply(data).setEphemeral(ephemeral);

    }

    // REPLY (INTELLIGENT) //

    @Override
    public @NotNull RestAction<?> reply(@NotNull String txt, boolean ephemeral) {
        if (ephemeral) {
            return send(txt, true);
        }

        if (!(event instanceof IReplyCallback callback)) {
            throw new IllegalStateException("No reply method applicable to `" + this + "`");
        }

        if (event.isAcknowledged()) {
            return editRaw(txt);
        } else {
            return send(txt, false);
        }
    }

    @Override
    public @NotNull RestAction<?> reply(@NotNull MessageCreateData data, boolean ephemeral) {
        if (ephemeral) {
            return send(data, true);
        }

        if (!(event instanceof IReplyCallback callback)) {
            throw new IllegalStateException("No reply method applicable to `" + this + "`");
        }

        if (event.isAcknowledged()) {
            return edit(MessageEditData.fromCreateData(data));
        } else {
            return send(data, false);
        }
    }

}
