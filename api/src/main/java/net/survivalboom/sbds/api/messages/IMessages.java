package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import net.survivalboom.sbds.api.messages.builder.MessageBuilder;
import net.survivalboom.sbds.api.messages.parsers.AbstractTextParser;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface IMessages extends IManager {

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

    @NotNull MessageActionBuilder createActionMessage(@NotNull String name, @NotNull User user, @NotNull Function<MessageCreateData, RestAction<?>> function);

    @NotNull MessageBuilder createMessageBuilder(@NotNull String name, @NotNull User user);

    //
    // MESSAGE OPERATIONS
    //

    @NotNull MessageActionBuilder reply(@NotNull Object thing, @NotNull String name, @NotNull User user);

    default @NotNull MessageActionBuilder reply(@NotNull Object thing, @NotNull String name, @NotNull Member member) {
        return reply(thing, name, member.getUser());
    }

    //
    // PARSING
    //

    @NotNull String parse(@NotNull String in, @NotNull AbstractTextParser<?, ?> parser);

    @NotNull String parseTranslations(@NotNull String in, @Nullable User user);


}
