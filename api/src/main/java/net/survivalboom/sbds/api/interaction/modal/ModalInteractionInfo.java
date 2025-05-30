package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.InteractionInfo;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.MessageActionBuilder;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ModalInteractionInfo extends InteractionInfo<ModalInteractionEvent> {

    private final Map<String, String> map = new HashMap<>();

    public ModalInteractionInfo(@NotNull ISBDS sbds, @NotNull Logger logger, @NotNull ModalInteractionEvent event) {
        super(event, sbds, logger);
        event.getInteraction().getValues().forEach(v -> map.put(v.getId(), v.getAsString()));
    }


    public @Nullable String value(@NotNull String id) {
        return map.get(id);
    }

    public @NotNull Map<String, String> values() {
        return new HashMap<>(map);
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

}
