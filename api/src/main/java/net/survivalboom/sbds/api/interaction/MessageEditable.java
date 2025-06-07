package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.MessageActionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MessageEditable {

    @NotNull IMessages messages();

    @NotNull IMessageEditCallback editCallback();

    @Nullable User user();

    default @NotNull MessageActionBuilder<MessageEditCallbackAction> edit(@NotNull String key) {
        return MessageActionBuilder.create(messages(), key, user(), d -> editCallback().editMessage(MessageEditData.fromCreateData(d)));
    }

    default @NotNull MessageEditCallbackAction editRaw(@NotNull String text) {
        return editCallback().editMessage(text);
    }

}
