package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import net.survivalboom.sbds.api.messages.builder.MessageBuilder;
import net.survivalboom.sbds.api.messages.parsers.AbstractTextParser;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface IMessages extends IManager, StringParser {

    @NotNull ISBDS getSbds();

    //
    // MESSAGES
    //

    @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable IUserData userData, boolean fallback);

    @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable User user, boolean fallback);

    @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable ITranslation translation, boolean fallback);

    @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable Guild guild, boolean fallback);

    default @Nullable IMessageTemplate getMessage(@NotNull String key, @NotNull InteractionHolder info, boolean fallback) {
        return getMessage(key, info.user(), fallback);
    }

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

    @Override
    default @NotNull String parse(@NotNull String string) {
        return parse(string, null);
    }

    @NotNull String parse(@NotNull String in, @Nullable AbstractTextParser<?, ?> parser);

    @NotNull String parseTranslations(@NotNull String in, @Nullable User user);


}
