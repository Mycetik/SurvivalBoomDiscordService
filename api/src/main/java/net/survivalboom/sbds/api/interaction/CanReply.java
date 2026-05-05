package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import org.jetbrains.annotations.NotNull;

public interface CanReply<TReplyCallback extends IReplyCallback> extends InteractionHolder<TReplyCallback> {

    default @NotNull ReplyCallbackAction replyRaw(@NotNull String text) {
        return interaction().reply(text);
    }

    default @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull String key) {
        return new MessageActionBuilder<>(messages(), user(), key, interaction()::reply);
    }

}
