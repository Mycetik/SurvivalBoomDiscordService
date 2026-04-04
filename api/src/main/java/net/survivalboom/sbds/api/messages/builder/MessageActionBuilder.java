package net.survivalboom.sbds.api.messages.builder;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.translations.IMessage;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class MessageActionBuilder<T extends FluentRestAction<?, ?>> extends AbstractMessageBuilder<MessageActionBuilder<T>> {

    private final Function<MessageCreateData, T> action;

    private MessageActionBuilder(
            @NotNull ISBDS sbds,
            @Nullable User user,
            @NotNull Function<AbstractMessageBuilder<MessageActionBuilder<T>>, MessageCreateData> messageDataSupplier,
            @NotNull Function<MessageCreateData, T> action
    ) {
        super(sbds, user, messageDataSupplier);
        this.action = action;
    }

    public T send() {
        MessageCreateData messageCreateData = createMessage();
        return action.apply(messageCreateData);
    }

    public void queue() {
        send().queue();
    }

    public void complete() {
        send().complete();
    }



    public static @NotNull <T extends FluentRestAction<?, ?>> MessageActionBuilder<T> create(@NotNull IMessages messages, @NotNull String key, @Nullable User user, @NotNull Function<MessageCreateData, T> action) {

        Function<AbstractMessageBuilder<MessageActionBuilder<T>>, MessageCreateData> supplier = b -> {

            IMessage message = messages.getMessage(key, user, true);
            if (message == null) {
                return MessageCreateData.fromContent(key);
            }

            return message.createMessageData(b::componentIdCreator, messages, b.placeholders);

        };

        return new MessageActionBuilder<>(messages.getSbds(), user, supplier, action);

    }

    public static @NotNull <T extends FluentRestAction<?, ?>> MessageActionBuilder<T> create(@NotNull IMessages messages, @NotNull String key, @Nullable Guild guild, @NotNull Function<MessageCreateData, T> action) {

        Function<AbstractMessageBuilder<MessageActionBuilder<T>>, MessageCreateData> supplier = b -> {

            IMessage message = messages.getMessage(key, guild, true);
            if (message == null) {
                return MessageCreateData.fromContent(key);
            }

            return message.createMessageData(b::componentIdCreator, messages, b.placeholders);

        };

        return new MessageActionBuilder<>(messages.getSbds(), null, supplier, action);

    }

}
