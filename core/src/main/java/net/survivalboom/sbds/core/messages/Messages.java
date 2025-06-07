package net.survivalboom.sbds.core.messages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.MessageActionBuilder;
import net.survivalboom.sbds.api.messages.MessageBuilder;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.database.Database;
import net.survivalboom.sbds.core.database.guilds.GuildRepositoryHandler;
import net.survivalboom.sbds.core.database.users.UserRepositoryHandler;
import net.survivalboom.sbds.core.translations.Translation;
import net.survivalboom.sbds.core.translations.TranslationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages extends Manager implements IMessages {

    private final SBDS sbds;

    private final TranslationManager translationManager;

    private final Database database;

    private UserRepositoryHandler userRepository;

    private GuildRepositoryHandler guildRepository;


    public Messages(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.translationManager = sbds.getTranslationManager();
        this.database = sbds.getDatabase();
    }

    @Override
    protected void init0() {
        this.userRepository = database.getRepositoryHandler("sbds:users", UserRepositoryHandler.class);
        this.guildRepository = database.getRepositoryHandler("sbds:guilds", GuildRepositoryHandler.class);
    }

    @Override
    protected void shutdown0() {
        this.userRepository = null;
    }


    //
    // MESSAGES
    //


    @Override
    public @NotNull SBDS getSbds() {
        return sbds;
    }

    @Override
    public @Nullable IMessage getMessage(@NotNull String name, @Nullable IUserData userData, boolean fallback) {

        checkValid();

        if (userData == null) return getMessage(name, (Translation) null, fallback);
        ITranslation translation = userData.translation();

        return getMessage(name, translation, fallback);

    }

    @Override
    public @Nullable IMessage getMessage(@NotNull String name, @Nullable User user, boolean fallback) {

        checkValid();

        if (user == null) return getMessage(name, (IUserData) null, fallback);

        IUserData userData = getUserData(user);

        return getMessage(name, userData, fallback);

    }

    @Override
    public @Nullable IMessage getMessage(@NotNull String name, @Nullable ITranslation translation, boolean fallback) {

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

    @Override
    public @Nullable IMessage getMessage(@NotNull String name, @Nullable Guild guild, boolean fallback) {

        IMessage message = null;
        if (guild != null) {
            ITranslation translation = guildRepository.createGuildData(guild).translation();
            if (translation != null) message = translation.getMessage(name);
        }

        if (!fallback || message != null) return message;

        message = getMessage1(name, translationManager.defaultTranslation());
        if (message != null) return message;

        message = getMessage1(name, translationManager.fallbackTranslation());

        return message;

    }

    private @Nullable IMessage getMessage1(@NotNull String name, @Nullable ITranslation translation) {
        if (translation == null) return null;
        return translation.getMessage(name);
    }

    private @NotNull IUserData getUserData(@NotNull User user) {
        return userRepository.createUser(user);
    }

    //
    // MESSAGE CREATORS
    //

    @Override
    public @NotNull <T extends FluentRestAction<?, ?>> MessageActionBuilder<T> createActionMessage(@NotNull String name, @Nullable User user, @NotNull Function<MessageCreateData, T> function) {
        return MessageActionBuilder.create(this, name, user, function);
    }

    @Override
    public @NotNull <T extends FluentRestAction<?, ?>> MessageActionBuilder<T> createActionMessage(@NotNull String name, @Nullable Guild guild, @NotNull Function<MessageCreateData, T> function) {
        return MessageActionBuilder.create(this, name, guild, function);
    }

    @Override
    public @NotNull MessageBuilder createMessageBuilder(@NotNull String name, @Nullable User user) {
        return MessageBuilder.create(this, name, user);
    }

    @Override
    public @NotNull MessageBuilder createMessageBuilder(@NotNull String name, @Nullable Guild guild) {
        return MessageBuilder.create(this, name, guild);
    }

    //
    // MESSAGE OPERATIONS
    //

    @Override
    public @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull IReplyCallback callback, @NotNull String name, @Nullable User user) {
        return MessageActionBuilder.create(this, name, user, callback::reply);
    }

    @Override
    public @NotNull MessageActionBuilder<MessageEditCallbackAction> edit(@NotNull IMessageEditCallback callback, @NotNull String name, @Nullable User user) {
        return MessageActionBuilder.create(this, name, user, d -> callback.editMessage(MessageEditData.fromCreateData(d)));
    }

    //
    // PARSER
    //

    @Override
    public @NotNull String parse(@NotNull String in, @NotNull Function<String, IMessage> supplier, @Nullable Placeholders placeholders) {

        checkValid();

        // Регулярное выражение для поиска плейсхолдеров вида [namespace.key] или [language:namespace.key]
        String regex = "\\[(.*?)]";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(in);
        StringBuilder parsedText = new StringBuilder();

        while (matcher.find()) {


            String found = matcher.group().replace("[", "").replace("]", "");
            IMessage translatedMessage;

            String[] args = found.split(":");
            if (args.length == 2) {

                String namespace = args[0];
                String key = args[1];

                Translation translation = translationManager.getTranslation(namespace);
                if (translation == null) continue;

                translatedMessage = translation.getMessage(key);

            }

            else {
                translatedMessage = supplier.apply(found);
            }

            if (translatedMessage == null) continue;

            matcher.appendReplacement(parsedText, translatedMessage.buildString(placeholders));

        }

        matcher.appendTail(parsedText);

        return Placeholders.parse(parsedText.toString(), placeholders);

    }

}
