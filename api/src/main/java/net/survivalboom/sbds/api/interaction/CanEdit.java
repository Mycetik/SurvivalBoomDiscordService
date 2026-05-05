package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import org.jetbrains.annotations.NotNull;

public interface CanEdit<TReplyCallback extends IMessageEditCallback> extends InteractionHolder<TReplyCallback> {

    default @NotNull MessageActionBuilder<MessageEditCallbackAction> edit(@NotNull String key) {
        return new MessageActionBuilder<>(messages(), user(), key, d -> interaction().editMessage(MessageEditData.fromCreateData(d)));
    }

    default @NotNull MessageEditCallbackAction editRaw(@NotNull String text) {
        return interaction().editMessage(text);
    }

}
