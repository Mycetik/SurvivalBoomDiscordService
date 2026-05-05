package net.survivalboom.sbds.api.messages.builder;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;

public class MessageBuilder extends AbstractMessageBuilder<MessageBuilder> {

    public MessageBuilder(
            @NotNull IMessages messages,
            @NotNull User user,
            @NotNull String messageKey
    ) {
        super(messages, user, messageKey);
    }

}
