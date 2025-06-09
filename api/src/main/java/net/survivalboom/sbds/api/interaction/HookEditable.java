package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.MessageActionBuilder;
import org.jetbrains.annotations.NotNull;

public interface HookEditable {

    @NotNull InteractionHook hook();

    @NotNull IMessages messages();

    @NotNull User user();

    default @NotNull WebhookMessageEditAction<Message> editHookRaw(@NotNull String text) {
        return hook().editOriginal(text);
    }

    default @NotNull MessageActionBuilder<WebhookMessageEditAction<Message>> editHook(@NotNull String key) {
        return MessageActionBuilder.create(messages(), key, user(), d -> hook().editOriginal(MessageEditData.fromCreateData(d)));
    }

}
