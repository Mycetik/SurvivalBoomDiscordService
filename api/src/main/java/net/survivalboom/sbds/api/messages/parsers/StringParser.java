package net.survivalboom.sbds.api.messages.parsers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface StringParser {

    @NotNull String parse(@NotNull String string);

    default @Nullable String parseNullable(@Nullable String string) {

        if (string == null) {
            return null;
        }

        return parse(string);

    }

    default @NotNull List<String> parseAll(@NotNull Collection<String> strings) {

        List<String> out = new ArrayList<>();
        for (String string : strings) {
            String parsedStr = parse(string);
            out.add(parsedStr);
        }

        return out;

    }



    static @Nullable String stParseNullable(@Nullable StringParser parser, @Nullable String text) {

        if (parser == null) {
            return text;
        }

        return parser.parseNullable(text);

    }

    static @NotNull String stParse(@Nullable StringParser parser, @NotNull String text) {

        if (parser == null) {
            return text;
        }

        return parser.parse(text);

    }

    static @NotNull String stParse(@NotNull String string, @NotNull Collection<StringParser> parsers) {

        for (StringParser parser : parsers) {
            string = parser.parse(string);
        }

        return string;

    }

}
