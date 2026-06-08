package net.survivalboom.sbds.core.messages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.messages.parsers.AbstractTextParser;
import net.survivalboom.sbds.api.messages.parsers.LinkedTextParser;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import net.survivalboom.sbds.api.messages.builder.MessageBuilder;
import net.survivalboom.sbds.api.messages.template.TextMessageTemplate;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.database.guilds.GuildDataManager;
import net.survivalboom.sbds.core.database.users.UserDataManager;
import net.survivalboom.sbds.core.translations.Translation;
import net.survivalboom.sbds.core.translations.TranslationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages extends Manager implements IMessages {

    private final SBDS sbds;

    private final TranslationManager translationManager;

    private final UserDataManager users;

    private final GuildDataManager guilds;


    public Messages(@NotNull SBDS sbds) {

        this.sbds = sbds;
        this.translationManager = sbds.getTranslationManager();

        this.users = sbds.getUserDataManager();
        this.guilds = sbds.getGuildDataManager();

    }

    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {

    }


    //
    // MESSAGES
    //


    @Override
    public @NotNull SBDS getSbds() {
        return sbds;
    }

    @Override
    public @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable IUserData userData, boolean fallback) {

        checkValid();

        if (userData == null) {
            return getMessage(name, (Translation) null, fallback);
        }

        ITranslation translation = userData.getTranslation();

        return getMessage(name, translation, fallback);

    }

    @Override
    public @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable User user, boolean fallback) {

        checkValid();

        if (user == null) return getMessage(name, (IUserData) null, fallback);

        IUserData userData = users.get(user).join();

        return getMessage(name, userData, fallback);

    }

    @Override
    public @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable ITranslation translation, boolean fallback) {

        IMessageTemplate message;
        if (translation != null) {
            message = translation.getMessage(name);
            if (message != null) return message;
        }

        if (!fallback) return null;

        message = getMessage1(name, translationManager.getDefaultTranslation());
        if (message != null) return message;

        message = getMessage1(name, translationManager.getFallbackTranslation());

        return message;

    }

    @Override
    public @Nullable IMessageTemplate getMessage(@NotNull String name, @Nullable Guild guild, boolean fallback) {

        IMessageTemplate message = null;
        if (guild != null) {

            IGuildData guildData = guilds.get(guild).join();
            if (guildData != null) {

                ITranslation translation = guildData.getTranslation();
                if (translation != null) {
                    message = translation.getMessage(name);
                }

            }

        }

        if (!fallback || message != null) return message;

        message = getMessage1(name, translationManager.getDefaultTranslation());
        if (message != null) return message;

        message = getMessage1(name, translationManager.getFallbackTranslation());

        return message;

    }

    private @Nullable IMessageTemplate getMessage1(@NotNull String name, @Nullable ITranslation translation) {
        if (translation == null) return null;
        return translation.getMessage(name);
    }

    //
    // MESSAGE CREATORS
    //

    @Override
    public @NotNull MessageActionBuilder createActionMessage(@NotNull String name, @NotNull User user, @NotNull Function<MessageCreateData, RestAction<?>> function) {
        return new MessageActionBuilder(this, user, name, function);
    }

    @Override
    public @NotNull MessageBuilder createMessageBuilder(@NotNull String name, @NotNull User user) {
        return new MessageBuilder(this, user, name);
    }

    //
    // MESSAGE OPERATIONS
    //

    @Override // Від цього методу смердить помиями, обережно!
    public @NotNull MessageActionBuilder reply(@NotNull Object thing, @NotNull String name, @NotNull User user) {

        Function<MessageCreateData, RestAction<?>> action;
        switch (thing) {

            case Message message -> action = message::reply;

            case TextChannel channel -> action = channel::sendMessage;

            case IReplyCallback replyCallback -> {

                if (replyCallback.isAcknowledged()) {
                    action = d -> replyCallback.getHook().editOriginal(MessageEditData.fromCreateData(d));
                } else {
                    action = replyCallback::reply;
                }

            }

            default -> throw new IllegalArgumentException("No reply method applicable to `" + thing + "`");

        }

        return new MessageActionBuilder(this, user, name, action);

    }

    //
    // PARSER
    //

    // Регулярное выражение для поиска плейсхолдеров вида [namespace.key] или [language:namespace.key]
    private final Pattern msgReferenceRegex = Pattern.compile("\\$\\[(.*?)]");

    @Override
    public @NotNull String parse(@NotNull String in, @Nullable AbstractTextParser<?, ?> parser) {

        Objects.requireNonNull(in, "in == null");
        checkValid();

        User user;
        if (parser instanceof LinkedTextParser linkedTextParser) {
            user = linkedTextParser.getTarget();
        }

        else {
            user = null;
        }

        String lastAttempt = in;
        while (true) {

            String attempt = parseTranslations(in, user);

            if (parser != null) {
                Placeholders placeholders = parser.getPlaceholders();
                var parsers = parser.getParsers();

                attempt = placeholders.parse(attempt);
                attempt = StringParser.stParse(attempt, parsers);
            }

            if (lastAttempt.equals(attempt)) {
                break;
            }

            lastAttempt = attempt;

        }

        return lastAttempt;

    }

    @Override
    public @NotNull String parseTranslations(@NotNull String in, @Nullable User user) {

        Objects.requireNonNull(in, "in == null");
        checkValid();

        Matcher matcher = msgReferenceRegex.matcher(in);
        StringBuilder parsedText = new StringBuilder();

        while (matcher.find()) {

            String found = matcher.group()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("$", "");

            IMessageTemplate translatedMessage;

            String[] args = found.split(":");
            if (args.length == 2) {

                String namespace = args[0];
                String key = args[1];

                ITranslation translation = translationManager.getTranslation(namespace);
                if (translation == null) {
                    continue;
                }

                translatedMessage = translation.getMessage(key);

            }

            else {
                translatedMessage = getMessage(found, user, true);
            }

            if (translatedMessage == null) {
                continue;
            }

            if (translatedMessage instanceof TextMessageTemplate template) {
                matcher.appendReplacement(parsedText, template.getContent());
            }

        }

        matcher.appendTail(parsedText);

        return parsedText.toString();

    }

}
