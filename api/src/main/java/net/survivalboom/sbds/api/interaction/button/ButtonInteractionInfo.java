package net.survivalboom.sbds.api.interaction.button;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.MessageActionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// TODO зробити нормальну абстракцію.
public class ButtonInteractionInfo {

    private final ISBDS sbds;

    private final ButtonInteractionEvent event;


    public ButtonInteractionInfo(@NotNull ISBDS sbds, @NotNull ButtonInteractionEvent event) {
        this.sbds = sbds;
        this.event = event;
    }



    public @NotNull ButtonInteractionEvent event() {
        return event;
    }

    public @Nullable Guild guild() {
        return this.event.getGuild();
    }

    public @Nullable Member member() {
        return this.event.getMember();
    }

    public @NotNull User user() {
        return this.event.getUser();
    }


    public @NotNull ISBDS sbds() {
        return sbds;
    }

    public @NotNull IMessages messages() {
        return sbds.getMessages();
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

    protected @NotNull IMessage getMessage(@NotNull String key) {
        return Objects.requireNonNull(messages().getMessage(key, user(), true));
    }

}
