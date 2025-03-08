package net.survivalboom.sbds.core.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.translations.ITranslationManager;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.database.users.UserData;
import net.survivalboom.sbds.core.database.users.UserRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Messages extends Manager implements IMessages {

    private final ITranslationManager translationManager;

    private final UserRepositoryHandler repository;


    public Messages(@NotNull SBDS sbds) {
        this.repository = sbds.getDatabase().getRepositoryHandler("sbds:users", UserRepositoryHandler.class);
        this.translationManager = sbds.getTranslationManager();
    }

    @Override
    protected void init0() {}

    @Override
    protected void shutdown0() {}


    @Override
    public @Nullable IMessage getMessage(@NotNull String name, @Nullable IUserData userData, boolean fallback) {

        if (userData == null) return getMessage0(name, null, fallback);
        ITranslation translation = userData.translation();

        return getMessage0(name, translation, fallback);

    }

    @Override
    public @Nullable IMessage getMessage(@NotNull String name, @Nullable User user, boolean fallback) {

        if (user == null) return getMessage(name, (IUserData) null, fallback);

        UserData userData = repository.createUser(user);

        return getMessage(name, userData, fallback);

    }

    @Override
    public @Nullable MessageCreateData getMessageData(@NotNull String name, @Nullable Placeholders placeholders, @Nullable IUserData userData, boolean fallback) {

        IMessage message = getMessage(name, userData, fallback);
        if (message == null) return null;

        return message.messageData(placeholders);

    }

    @Override
    public @Nullable MessageCreateData getMessageData(@NotNull String name, @Nullable Placeholders placeholders, @Nullable User user, boolean fallback) {

        IMessage message = getMessage(name, user, fallback);
        if (message == null) return null;

        return message.messageData(placeholders);

    }

    @Override
    public @NotNull MessageCreateData getMessageDataFallback(@NotNull String name,  @Nullable Placeholders placeholders, @Nullable IUserData userData) {

        MessageCreateData createData = getMessageData(name, placeholders, userData, true);
        if (createData != null) return createData;

        return MessageCreateData.fromContent(name);

    }

    @Override
    public @NotNull MessageCreateData getMessageDataFallback(@NotNull String name,  @Nullable Placeholders placeholders, @Nullable User user) {

        MessageCreateData createData = getMessageData(name, placeholders, user, true);
        if (createData != null) return createData;

        return MessageCreateData.fromContent(name);

    }

    private @Nullable IMessage getMessage0(@NotNull String name, @Nullable ITranslation translation, boolean fallback) {

        IMessage message;
        if (translation != null) {
            message = translation.getMessage(name);
            if (message != null) return message;
        }

        if (!fallback) return null;

        message = getMessage1(name, translationManager.defaultTranslation());
        if (message != null) return message;

        message = getMessage1(name, translationManager.fallbackTranslation());

        return message;

    }

    private @Nullable IMessage getMessage1(@NotNull String name, @Nullable ITranslation translation) {
        if (translation == null) return null;
        return translation.getMessage(name);
    }


    @Override
    public @NotNull ReplyCallbackAction reply(@NotNull SlashCommandInteraction interaction, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user) {
        MessageCreateData data = getMessageDataFallback(name, placeholders, user);
        return interaction.reply(data);
    }

    @Override
    public @NotNull ReplyCallbackAction reply(@NotNull SlashCommandInteraction interaction, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData userData) {
        MessageCreateData data = getMessageDataFallback(name, placeholders, userData);
        return interaction.reply(data);
    }

    @Override
    public @NotNull MessageCreateAction reply(@NotNull Message message, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user) {
        MessageCreateData data = getMessageDataFallback(name, placeholders, user);
        return message.reply(data);
    }

    @Override
    public @NotNull MessageCreateAction reply(@NotNull Message message, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData userData) {
        MessageCreateData data = getMessageDataFallback(name, placeholders, userData);
        return message.reply(data);
    }


    @Override
    public @NotNull MessageCreateAction sendMessage(@NotNull TextChannel channel, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user) {
        MessageCreateData data = getMessageDataFallback(name, placeholders, user);
        return channel.sendMessage(data);
    }

    @Override
    public @NotNull MessageCreateAction sendMessage(@NotNull TextChannel channel, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData userData) {
        MessageCreateData data = getMessageDataFallback(name, placeholders, userData);
        return channel.sendMessage(data);
    }

}
