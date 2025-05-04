package net.survivalboom.sbds.core.commands.slash;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentResources;
import net.survivalboom.sbds.api.utils.TypeMap;
import net.survivalboom.sbds.core.commands.AbstractCommandParser;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SlashCommandParser extends AbstractCommandParser {

    private final SlashCommandInteraction interaction;

    public SlashCommandParser(@NotNull Command command, @NotNull ArgumentResources resources, @NotNull SlashCommandInteraction interaction) {
        super(command, resources);
        this.interaction = interaction;
    }


    @Override
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

}
