package net.survivalboom.sbds.api.messages.parsers;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class LinkedTextParser extends AbstractTextParser<LinkedTextParser, LinkedTextParser.Builder> implements StringParser {

    private final @NotNull IMessages messages;

    private final @NotNull User target;

    public LinkedTextParser(
            @NotNull IMessages messages,
            @NotNull User target,
            @NotNull List<StringParser> parsers,
            @NotNull Placeholders placeholders
    ) {
        super(parsers, placeholders);

        Objects.requireNonNull(messages, "messages == null");
        Objects.requireNonNull(target, "target == null");

        this.messages = messages;
        this.target = target;

    }

    public @NotNull IMessages getMessages() {
        return messages;
    }

    public @NotNull User getTarget() {
        return target;
    }

    //
    // PARSERS
    //

    public @NotNull String multiParse(@NotNull String string, @Nullable AbstractTextParser<?, ?>... parsers) {

        string = parse(string);

        for (var parser : parsers) {

            if (parser == null) {
                continue;
            }

            string = messages.parse(string, parser);
        }

        return string;

    }

    public @NotNull List<String> multiParse(@NotNull Collection<String> strings, @Nullable AbstractTextParser<?, ?>... parsers) {

        List<String> out = parseAll(strings);

        for (var parser : parsers) {

            if (parser == null) {
                continue;
            }

            out.replaceAll(text -> messages.parse(text, parser));

        }

        return out;

    }

    @Override
    public @NotNull String parse(@NotNull String string) {
        return messages.parse(string, this);
    }

    //
    // COPY
    //

    @Override
    public @NotNull LinkedTextParser.Builder copy() {
        return new Builder(this);
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder(@NotNull IMessages messages, @NotNull User user) {
        Objects.requireNonNull(messages, "messages == null");
        Objects.requireNonNull(user, "user == null");
        return new Builder(messages, user);
    }

    public static @NotNull Builder fromParser(@NotNull IMessages messages, @NotNull User user, @NotNull AbstractTextParser<?, ?> parser) {

        var builder = builder(messages, user);

        builder
            .addParsers(parser.parsers)
            .addPlaceholders(parser.placeholders);

        return builder;

    }

    public static class Builder extends AbstractTextParser.Builder<LinkedTextParser, Builder> {

        private final @NotNull IMessages messages;

        private final @NotNull User target;


        protected Builder(
                @NotNull IMessages messages,
                @NotNull User target
        ) {
            this.messages = messages;
            this.target = target;
        }

        protected Builder(@NotNull Builder builder) {
            super(builder);
            this.messages = builder.messages;
            this.target = builder.target;
        }

        protected Builder(@NotNull LinkedTextParser parser) {
            super(parser);
            this.messages = parser.messages;
            this.target = parser.target;
        }

        public @NotNull IMessages getMessages() {
            return messages;
        }

        public @NotNull User getTarget() {
            return target;
        }

        // BUILD //

        @Override
        public @NotNull LinkedTextParser build() {
            return new LinkedTextParser(messages, target, parsers, placeholders);
        }

        @Override
        public @NotNull LinkedTextParser.Builder copy() {
            return new Builder(this);
        }

    }

}
