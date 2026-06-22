package net.survivalboom.sbds.api.commands.argument.primitive;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class GreedyStringArgument extends Argument<String> {

    private final Function<CommandAutoCompleteInteractionEvent, List<Command.Choice>> autocomplete;

    public GreedyStringArgument(@Nullable Function<CommandAutoCompleteInteractionEvent, List<Command.Choice>> autocomplete) {
        this.autocomplete = autocomplete;
    }

    public GreedyStringArgument() {
        this(null);
    }

    @Override
    public @NotNull String parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        if (input instanceof String string) {
            return string;
        }

        else if (input instanceof OptionMapping mapping) {
            return mapping.getAsString();
        }

        throw new ArgumentParseException();

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

    @Override
    public boolean isAutoComplete() {
        return autocomplete != null;
    }

    @Override
    public int split(@NotNull String input) {
        return input.length();
    }

}
