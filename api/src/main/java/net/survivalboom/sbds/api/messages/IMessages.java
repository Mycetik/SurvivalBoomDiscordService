package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface IMessages {

    @NotNull ISBDS getSbds();

    //
    // MESSAGES
    //

    @Nullable IMessage getMessage(@NotNull String name, @Nullable IUserData userData, boolean fallback);

    @Nullable IMessage getMessage(@NotNull String name, @Nullable User user, boolean fallback);

    @Nullable IMessage getMessage(@NotNull String name, @Nullable ITranslation translation, boolean fallback);

    //
    // MESSAGE CREATORS
    //

    @NotNull <T extends FluentRestAction<?, ?>> MessageActionBuilder<T> createActionMessage(@NotNull String name, @Nullable User user, @NotNull Function<MessageCreateData, T> function);

    //
    // MESSAGE OPERATIONS
    //

    @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull IReplyCallback callback, @NotNull String name, @Nullable User user);

    //
    // PARSING
    //

    @NotNull String parse(@NotNull String in, @NotNull Function<String, IMessage> supplier, @Nullable Placeholders placeholders);


}
