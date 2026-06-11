package net.survivalboom.sbds.core.commands.parser;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import net.survivalboom.sbds.api.utils.typemap.UnmodifiableTypeMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommandParser {

    public static @NotNull TypeMap parse(
            @NotNull ISlashCommandManager.IRegisteredSlashCommand rootCommand,
            @NotNull Command currentCommand,
            @NotNull SlashCommandInteraction interaction
    ) throws ArgumentParsingException {

        List<CommandArgument> arguments = currentCommand.getArguments().stream()
                .filter(argument -> argument.scopes().contains(ArgumentScope.SLASH))
                .toList();

        Map<String, Object> map = new HashMap<>();
        for (CommandArgument argument : arguments) {

            OptionMapping mapping = interaction.getOption(argument.name());
            if (mapping == null) {
                continue;
            }

            ArgumentParsingContext context = new ArgumentParsingContext(rootCommand, currentCommand, argument);

            Object object;
            try {
                object = argument.argument().parse(mapping, context);
            } catch (ArgumentParseException e) {
                throw new ArgumentParsingException(argument, mapping.getAsString(), e);
            }

            map.put(argument.name(), object);

        }

        return UnmodifiableTypeMap.ofMap(map);

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
