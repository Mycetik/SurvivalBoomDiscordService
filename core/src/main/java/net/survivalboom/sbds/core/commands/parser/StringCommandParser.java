package net.survivalboom.sbds.core.commands.parser;

import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import net.survivalboom.sbds.api.utils.typemap.UnmodifiableTypeMap;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StringCommandParser {


    public static @NotNull Result parseInput(
            @NotNull String input,
            @NotNull ArgumentScope scope,
            @NotNull List<CommandArgument> arguments,
            @NotNull Function<CommandArgument, ArgumentParsingContext> contextCreator
    ) throws ArgumentParsingException {

        var splitResult = splitToParts(input, scope, arguments);
        var parseResult = parseArgumentParts(splitResult.splitArguments, contextCreator);

        return new Result(parseResult.arguments, parseResult.arguments2, splitResult.remaining);

    }


    private static @NotNull SplitResult splitToParts(
            @NotNull String input,
            @NotNull ArgumentScope scope,
            @NotNull List<CommandArgument> arguments
    ) {

        if (input.isBlank()) {
            throw new IllegalArgumentException("input is blank");
        }

        List<CommandArgument> argumentsSorted = arguments.stream()
                .filter(argument -> argument.scopes().contains(scope))
                .sorted(Comparator.comparing(argument -> {

                    int index = argument.index();
                    if (!argument.required()) {
                        index += 100;
                    }

                    return index;

                }))
                .toList();

        Map<CommandArgument, String> parts = new HashMap<>();
        String text = input;
        for (CommandArgument commandArgument : argumentsSorted) {

            Argument<?> argument = commandArgument.argument();

            int endIndex = argument.split(text);
            String part = text.substring(0, endIndex).strip();
            if (part.isBlank()) {
                throw new IllegalArgumentException("Part for `" + commandArgument.name() + "` is blank!");
            }

            parts.put(commandArgument, part);

            if (endIndex >= text.length()) {
                text = "";
                break;
            }

            text = text.substring(endIndex + 1);

        }

        return new SplitResult(parts, text);


    }

    private static @NotNull ParseResult parseArgumentParts(
            @NotNull Map<CommandArgument, String> map,
            @NotNull Function<CommandArgument, ArgumentParsingContext> contextFunction
    ) throws ArgumentParsingException {

        Map<String, Object> out = new HashMap<>();
        Map<CommandArgument, Object> out2 = new HashMap<>();

        for (Map.Entry<CommandArgument, String> entry : map.entrySet()) {

            CommandArgument argument = entry.getKey();
            String raw = entry.getValue();

            ArgumentParsingContext context = contextFunction.apply(argument);

            Object object;
            try {
                object = argument.argument().parse(raw, context);
            }

            catch (Exception e) {
                throw new ArgumentParsingException(argument, raw, e);
            }

            out.put(argument.name(), object);
            out2.put(argument, object);

        }

        return new ParseResult(UnmodifiableTypeMap.ofMap(out), out2);

    }

    public static @NotNull String getPrefix(@NotNull String input) {

        String[] args = input.split(" ");
        if (args.length == 0) return input;

        return args[0];

    }


    private record SplitResult(@NotNull Map<CommandArgument, String> splitArguments, @NotNull String remaining) {}

    private record ParseResult(@NotNull TypeMap arguments, @NotNull Map<CommandArgument, Object> arguments2) {}

    public record Result(@NotNull TypeMap arguments, @NotNull Map<CommandArgument, Object> arguments2, @NotNull String remaining) {}

}
