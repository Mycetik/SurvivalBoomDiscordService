package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import org.jetbrains.annotations.NotNull;

public interface MessageReplyable {

    @NotNull IReplyCallback replyCallback();

    @NotNull User user();

    @NotNull IMessages messages();


    default @NotNull ReplyCallbackAction replyRaw(@NotNull String text) {
        return replyCallback().reply(text);
    }

    default @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull String key) {
        return MessageActionBuilder.create(messages(), key, user(), replyCallback()::reply);
    }

}
