package net.survivalboom.sbds.core.commands.parser;

import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import net.survivalboom.sbds.api.utils.typemap.UnmodifiableTypeMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StringCommandParser {


    public static @NotNull Result parseInput(
            @NotNull String input,
            @NotNull Command command,
            @NotNull ArgumentScope scope,
            @NotNull Function<CommandArgument, ArgumentParsingContext> contextCreator
    ) throws ArgumentParsingException, NotEnoughArgumentsException {

        var result = parseInput0(input, scope, command.getArguments(), contextCreator);

        Map<String, Object> args = result.arguments.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        TypeMap map = UnmodifiableTypeMap.ofMap(args);

        return new Result(map, result.arguments, result.foundSubcommands);

    }


    private static ParsingResult parseInput0(
            @NotNull String input,
            @NotNull ArgumentScope scope,
            @NotNull List<CommandArgument> arguments,
            @NotNull Function<CommandArgument, ArgumentParsingContext> contextCreator
    ) throws ArgumentParsingException, NotEnoughArgumentsException {

        input = input.trim();

        List<CommandArgument> argumentsSorted = arguments.stream()
                .filter(argument -> argument.scopes().contains(scope))
                .collect(Collectors.toList());

        var splitResult = splitToParts(input, argumentsSorted);
        if (splitResult.splitArguments.size() < argumentsSorted.size()) {
            throw new NotEnoughArgumentsException(splitResult.splitArguments, argumentsSorted);
        }

        var parsingResult = parseArgumentParts(splitResult.splitArguments, contextCreator);

        List<SubCommandArgument.SubCommand> subcommands = parsingResult.entrySet().stream()
                .filter(entry -> entry.getKey().argument() instanceof SubCommandArgument)
                .map(entry -> (SubCommandArgument.SubCommand) entry.getValue())
                .toList();

        List<SubCommandArgument.SubCommand> foundSubcommands = new ArrayList<>(subcommands);

        String remaining = splitResult.remaining;
        for (SubCommandArgument.SubCommand subcommand : subcommands) {

            ParsingResult result;
            try {
                result = parseInput0(remaining, scope, subcommand.command().getArguments(), contextCreator);
            }

            catch (NotEnoughArgumentsException e) {

                var got = splitResult.splitArguments;
                var expected = argumentsSorted;

                got.putAll(e.got);
                expected.addAll(e.expected);

                throw new NotEnoughArgumentsException(got, expected);

            }

            remaining = result.remaining;
            parsingResult.putAll(result.arguments);
            foundSubcommands.addAll(result.foundSubcommands);

        }

        return new ParsingResult(parsingResult, remaining, foundSubcommands);

    }


    private static @NotNull SplitResult splitToParts(
            @NotNull String input,
            @NotNull List<CommandArgument> arguments
    ) {

        if (input.isBlank()) {
            return new SplitResult(new HashMap<>(), "");
        }

        Map<CommandArgument, String> parts = new HashMap<>();
        String text = input;
        for (CommandArgument commandArgument : arguments) {

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

    private static @NotNull Map<CommandArgument, Object> parseArgumentParts(
            @NotNull Map<CommandArgument, String> map,
            @NotNull Function<CommandArgument, ArgumentParsingContext> contextFunction
    ) throws ArgumentParsingException {

        Map<CommandArgument, Object> out = new HashMap<>();

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

            out.put(argument, object);

        }

        return out;

    }

    public static @NotNull String getPrefix(@NotNull String input) {

        String[] args = input.split(" ");
        if (args.length == 0) return input;

        return args[0];

    }


    private record ParsingResult(@NotNull Map<CommandArgument, Object> arguments, @NotNull String remaining, @NotNull List<SubCommandArgument.SubCommand> foundSubcommands) {}

    private record SplitResult(@NotNull Map<CommandArgument, String> splitArguments, @NotNull String remaining) {}

    public record Result(@NotNull TypeMap arguments, @NotNull Map<CommandArgument, Object> arguments2, @NotNull List<SubCommandArgument.SubCommand> foundSubcommands) {}


    public static class NotEnoughArgumentsException extends Exception {

        public final Map<CommandArgument, String> got;

        public final List<CommandArgument> expected;


        public NotEnoughArgumentsException(
                @NotNull Map<CommandArgument, String> got,
                @NotNull List<CommandArgument> expected
        ) {
            this.got = got;
            this.expected = expected;
        }

    }

    public static class ArgumentParsingException extends Exception {

        private final CommandArgument argument;

        private final String input;

        public ArgumentParsingException(@NotNull CommandArgument argument, @NotNull String input, Exception e) {
            super(e);
            this.argument = argument;
            this.input = input;
        }

        public @NotNull CommandArgument getArgument() {
            return argument;
        }

        public @NotNull String getInput() {
            return input;
        }

    }

}
