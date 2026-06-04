package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface CanReply extends InteractionHolder {

    @ApiStatus.Internal
    @NotNull IReplyCallback replyCallback0();

    default @NotNull ReplyCallbackAction replyRaw(@NotNull String text) {
        return replyCallback0().reply(text);
    }

    default @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull String key) {
        return new MessageActionBuilder<>(messages(), user(), key, replyCallback0()::reply);
    }

}
