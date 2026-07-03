package net.survivalboom.sbds.api.messages.parsers;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TextParser extends AbstractTextParser<TextParser, TextParser.Builder> {

    public TextParser(
            @NotNull List<StringParser> parsers,
            @NotNull Placeholders placeholders
    ) {
        super(parsers, placeholders);
    }

    //
    // STATIC PARSERS
    //

    public @NotNull String multiParse(
            @NotNull IMessages messages,
            @NotNull String string,
            @Nullable AbstractTextParser<?, ?>... parsers
    ) {

        string = parse(messages, string);

        for (var parser : parsers) {

            if (parser == null) {
                continue;
            }

            string = messages.parse(string, parser);
        }

        return string;

    }

    public @NotNull List<String> multiParse(
            @NotNull IMessages messages,
            @NotNull Collection<String> strings,
            @Nullable AbstractTextParser<?, ?>... parsers
    ) {

        List<String> out = parseAll(messages, strings);

        for (var parser : parsers) {

            if (parser == null) {
                continue;
            }

            out.replaceAll(text -> messages.parse(text, parser));

        }

        return out;

    }

    public @NotNull StringParser createStringParser(@NotNull IMessages messages) {
        return s -> parse(messages, s);
    }

    public @NotNull String parse(@NotNull IMessages messages, @NotNull String string) {
        return messages.parse(string, this);
    }

    public @NotNull List<String> parseAll(@NotNull IMessages messages, @NotNull Collection<String> strings) {
        return strings.stream().map(str -> parse(messages, str)).collect(Collectors.toList());
    }

    //
    // COPY
    //

    @Override
    public @NotNull TextParser.Builder copy() {
        return new Builder(this);
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull Builder ofParser(@NotNull AbstractTextParser<?, ?> parser) {

        var builder = builder();

        builder
            .addParsers(parser.parsers)
            .addPlaceholders(parser.placeholders);

        return builder;

    }

    public static class Builder extends AbstractTextParser.Builder<TextParser, Builder> {

        protected Builder() {

        }

        protected Builder(@NotNull Builder builder) {
            super(builder);
        }

        protected Builder(@NotNull TextParser parser) {
            super(parser);
        }

        @Override
        public @NotNull TextParser build() {
            return new TextParser(parsers, placeholders);
        }

        @Override
        public @NotNull TextParser.Builder copy() {
            return new Builder(this);
        }

    }

}
