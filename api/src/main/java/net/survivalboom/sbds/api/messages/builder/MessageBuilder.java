package net.survivalboom.sbds.api.messages.builder;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.translations.IMessage;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class MessageBuilder extends AbstractMessageBuilder<MessageBuilder> {

    private MessageBuilder(@NotNull ISBDS sbds, @Nullable User user, @NotNull Function<AbstractMessageBuilder<MessageBuilder>, MessageCreateData> messageDataSupplier) {
        super(sbds, user, messageDataSupplier);
    }

    public @NotNull MessageCreateData build() {
        return createMessage();
    }


    public static @NotNull MessageBuilder create(@NotNull IMessages messages, @NotNull String key, @Nullable User user) {

        Function<AbstractMessageBuilder<MessageBuilder>, MessageCreateData> supplier = b -> {

            IMessage message = messages.getMessage(key, user, true);
            if (message == null) {
                return MessageCreateData.fromContent(key);
            }

            return message.createMessageData(b::componentIdCreator, messages, b.placeholders);

        };

        return new MessageBuilder(messages.getSbds(), user, supplier);

    }

    public static @NotNull MessageBuilder create(@NotNull IMessages messages, @NotNull String key, @Nullable Guild guild) {

        Function<AbstractMessageBuilder<MessageBuilder>, MessageCreateData> supplier = b -> {

            IMessage message = messages.getMessage(key, guild, true);
            if (message == null) {
                return MessageCreateData.fromContent(key);
            }

            return message.createMessageData(b::componentIdCreator, messages, b.placeholders);

        };

        return new MessageBuilder(messages.getSbds(), null, supplier);

    }

}
