package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MessageBuilder extends AbstractMessageBuilder<MessageBuilder> {

    public MessageBuilder(@NotNull ISBDS sbds) {
        super(sbds);
    }

    public @NotNull MessageCreateData build(@NotNull IMessage message) {
        Objects.requireNonNull(message, "message == null");
        return message.build(this::componentIdCreator, sbds.getMessages(), placeholders);
    }

}
