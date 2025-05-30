package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.messages.MessageActionBuilder;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class ComponentInteractionInfo<E extends GenericComponentInteractionCreateEvent> extends InteractionInfo<E> {

    public ComponentInteractionInfo(@NotNull E event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }

    public @NotNull ReplyCallbackAction replyRaw(@NotNull String text) {
        return event.reply(text);
    }

    public @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull String key) {
        return MessageActionBuilder.create(messages(), key, user(), event::reply);
    }

    public @NotNull WebhookMessageEditAction<Message> editRaw(@NotNull String text) {
        return event.getHook().editOriginal(text);
    }

    public @NotNull MessageActionBuilder<WebhookMessageEditAction<Message>> edit(@NotNull String key) {
        return MessageActionBuilder.create(messages(), key, user(), d -> event.getHook().editOriginal(MessageEditData.fromCreateData(d)));
    }

    public @NotNull ModalActionBuilder replyModal(@NotNull String key) {
        return new ModalActionBuilder(sbds.getModalInteractionManager(), event, NamespacedKey.fromString(key));
    }

}
