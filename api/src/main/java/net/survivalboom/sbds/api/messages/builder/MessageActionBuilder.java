package net.survivalboom.sbds.api.messages.builder;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MessageActionBuilder extends AbstractMessageBuilder<MessageActionBuilder> {

    private final Function<MessageCreateData, RestAction<?>> action;

    private boolean ephemeral = false;

    private boolean tts = false;

    private boolean silent = false;

    public MessageActionBuilder(
            @NotNull IMessages messages,
            @NotNull User user,
            @NotNull String messageKey,
            @NotNull Function<MessageCreateData, RestAction<?>> action
    ) {
        super(messages, user, messageKey);
        this.action = action;
    }

    public MessageActionBuilder setEphemeral(boolean v) {
        this.ephemeral = v;
        return this;
    }

    public MessageActionBuilder setTTS(boolean value) {
        this.tts = value;
        return this;
    }

    public MessageActionBuilder setSilent(boolean value) {
        this.silent = value;
        return this;
    }

    public void queue() {

        MessageCreateData messageCreateData = build();
        var rest = action.apply(messageCreateData);

        if (rest instanceof ReplyCallbackAction replyCallbackAction) {
            replyCallbackAction.setEphemeral(ephemeral);
        }

        if (rest instanceof MessageCreateRequest<?> messageCreateRequest) {
            messageCreateRequest.setTTS(tts).setSuppressedNotifications(silent);
        }

        rest.queue();

    }

}
