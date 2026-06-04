package net.survivalboom.sbds.api.messages.builder;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MessageActionBuilder<T extends RestAction<?>> extends AbstractMessageBuilder<MessageActionBuilder<T>> {

    private final Function<MessageCreateData, T> action;

    public MessageActionBuilder(
            @NotNull IMessages messages,
            @NotNull User user,
            @NotNull String messageKey,
            @NotNull Function<MessageCreateData, T> action
    ) {
        super(messages, user, messageKey);
        this.action = action;
    }

    public T send() {
        MessageCreateData messageCreateData = build();
        return action.apply(messageCreateData);
    }

    public void queue() {
        send().queue();
    }

    public void complete() {
        send().complete();
    }

}
