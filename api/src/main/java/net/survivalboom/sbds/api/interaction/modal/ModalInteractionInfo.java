package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

// Я би міг зробити це через абстракцію, але JDA не має нормального інтерфейсу для запихування туди усіх можливих Interaction.
public class ModalInteractionInfo {

    private final ISBDS sbds;

    private final Map<String, String> values = new HashMap<>();

    private final ModalInteraction modal;

    public ModalInteractionInfo(@NotNull ISBDS sbds, @NotNull ModalInteraction modal) {
        this.sbds = sbds;
        this.modal = modal;
        modal.getValues().forEach(v -> values.put(v.getId(), v.getAsString()));
    }


    public @NotNull ModalInteraction interaction() {
        return modal;
    }

    public @NotNull IMessages messages() {
        return sbds.getMessages();
    }

    public @NotNull ISBDS sbds() {
        return sbds;
    }


    public @Nullable Guild guild() {
        return modal.getGuild();
    }

    public @Nullable Member member() {
        return modal.getMember();
    }

    public @NotNull User user() {
        return modal.getUser();
    }


    public @Nullable String value(@NotNull String id) {
        return values.get(id);
    }

    public @NotNull Map<String, String> values() {
        return new HashMap<>(values);
    }


    public @NotNull ReplyCallbackAction reply(@NotNull String name, @Nullable Placeholders placeholders) {
        return messages().reply(modal, placeholders, name, user());
    }

    public @NotNull ReplyCallbackAction reply(@NotNull String name) {
        return messages().reply(modal, null, name, user());
    }


    public @NotNull WebhookMessageEditAction<Message> edit(@NotNull String name, @Nullable Placeholders placeholders) {
        return messages().edit(modal.getHook(), placeholders, name, user());
    }

    public @NotNull WebhookMessageEditAction<Message> edit(@NotNull String name) {
        return messages().edit(modal.getHook(), null, name, user());
    }

    public @NotNull ReplyCallbackAction deferReply() {
        return modal.deferReply();
    }

    public @NotNull MessageEditCallbackAction deferEdit() {
        return modal.deferEdit();
    }

}
