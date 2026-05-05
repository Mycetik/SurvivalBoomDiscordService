package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import net.survivalboom.sbds.api.messages.builder.MessageBuilder;
import net.survivalboom.sbds.api.messages.parsers.AbstractTextParser;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface IMessages {

    @NotNull ISBDS getSbds();

    //
    // MESSAGES
    //

    @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable IUserData userData, boolean fallback);

    @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable User user, boolean fallback);

    @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable ITranslation translation, boolean fallback);

    @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable Guild guild, boolean fallback);

    //
    // MESSAGE CREATORS
    //

    @NotNull <T extends FluentRestAction<?, ?>> MessageActionBuilder<T> createActionMessage(@NotNull String name, @Nullable User user, @NotNull Function<MessageCreateData, T> function);

    @NotNull <T extends FluentRestAction<?, ?>> MessageActionBuilder<T> createActionMessage(@NotNull String name, @Nullable Guild user, @NotNull Function<MessageCreateData, T> function);

    @NotNull MessageBuilder createMessageBuilder(@NotNull String name, @Nullable User user);

    @NotNull MessageBuilder createMessageBuilder(@NotNull String name, @Nullable Guild guild);

    //
    // MESSAGE OPERATIONS
    //

    @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull IReplyCallback callback, @NotNull String name, @Nullable User user);

    default @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull IReplyCallback callback, @NotNull String name, @Nullable Member member) {
        return reply(callback, name, member.getUser());
    }

    @NotNull MessageActionBuilder<MessageEditAction> editMessage(@NotNull Message message, @NotNull String name, @Nullable User user);

    @NotNull MessageActionBuilder<MessageCreateAction> sendMessage(@NotNull MessageChannel channel, @NotNull String name, @Nullable User user);

    //
    // PARSING
    //

    @NotNull String parse(@NotNull String in, @NotNull AbstractTextParser<?, ?> parser);


}
