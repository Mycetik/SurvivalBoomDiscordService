package net.survivalboom.sbds.core.commands;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentResources;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SlashCommandParser {

    private final Command command;

    private final ArgumentResources resources;

    private final SlashCommandInteraction interaction;

    private TypeMap arguments;

    public SlashCommandParser(@NotNull Command command, @NotNull ArgumentResources resources, @NotNull SlashCommandInteraction interaction) {
        this.command = command;
        this.resources = resources;
        this.interaction = interaction;
    }

    public boolean checkCount() {

        Objects.requireNonNull(arguments, "arguments == null");

        List<CommandArgument> requiredArguments = command.requiredArguments();

        return requiredArguments.stream().allMatch(a -> arguments.containsKey(a.name()));

    }

    public void parse() throws ArgumentParseException {

        Map<String, Object> map = new HashMap<>();

        for (CommandArgument argument : command.arguments()) {

            OptionMapping mapping = interaction.getOption(argument.name());
            if (mapping == null) continue;

            Object object = argument.argument().parse(mapping, resources);

            map.put(argument.name(), object);

        }

        arguments = TypeMap.ofMap(map, false);

    }

    public @NotNull TypeMap getArguments() {
        Objects.requireNonNull(arguments, "input wasn't parsed yet");
        return arguments;
    }

}
