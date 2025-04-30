package net.survivalboom.sbds.core.commands.string;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentResources;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StringCommandParser {

    private final String input;

    private final Command command;

    private final ArgumentResources resources;

    private TypeMap arguments;

    public StringCommandParser(@NotNull String input, @NotNull Command command, @NotNull ArgumentResources resources) {

        Objects.requireNonNull(input, "input == null");
        Objects.requireNonNull(command, "command == null");

        this.input = input;
        this.command = command;
        this.resources = resources;

    }
    
    
    public void parse() throws ArgumentParseException {

        if (input.isBlank()) {
            this.arguments = TypeMap.empty(false);
            return;
        }

        Map<CommandArgument, String> map = splitToParts(command, input);
        this.arguments = parseArgumentParts(map);
        
    }


    public boolean checkCount() {

        Objects.requireNonNull(arguments, "arguments == null");

        List<CommandArgument> requiredArguments = command.requiredArguments();

        return requiredArguments.stream().allMatch(a -> arguments.containsKey(a.name()));

    }
    


    public @NotNull TypeMap getArguments() {
        Objects.requireNonNull(arguments, "input wasn't parsed yet");
        return arguments;
    }

    public @NotNull String getInput() {
        return input;
    }


    private @NotNull Map<CommandArgument, String> splitToParts(@NotNull Command command, @NotNull String input) {

        Map<CommandArgument, String> parts = new HashMap<>();
        if (input.isBlank()) return parts;

        String text = input;
        for (CommandArgument commandArgument : command.arguments()) {

            Argument<?> argument = commandArgument.argument();

            int endIndex = argument.split(text);

            String part = text.substring(0, endIndex);

            parts.put(commandArgument, part);

            if (endIndex >= text.length()) break;

            text = text.substring(endIndex + 1);

        }

        return parts;


    }

    private @NotNull TypeMap parseArgumentParts(@NotNull Map<CommandArgument, String> map) throws ArgumentParseException {

        Map<String, Object> out = new HashMap<>();

        for (Map.Entry<CommandArgument, String> entry : map.entrySet()) {

            CommandArgument argument = entry.getKey();
            String raw = entry.getValue();

            Object object = argument.argument().parse(raw, resources);

            out.put(argument.name(), object);

        }

        return TypeMap.ofMap(out, false);

    }

    public static @NotNull String getPrefix(@NotNull String input) {

        String[] args = input.split(" ");
        if (args.length == 0) return input;

        return args[0];

    }

}
