package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MessageBuilder extends AbstractMessageBuilder<MessageBuilder> {

    public MessageBuilder(@NotNull ISBDS sbds, @Nullable User user) {
        super(sbds, user);
    }

    public @NotNull MessageCreateData build(@NotNull IMessage message) {
        Objects.requireNonNull(message, "message == null");
        return message.build(this::componentIdCreator, sbds.getMessages(), placeholders);
    }

}
