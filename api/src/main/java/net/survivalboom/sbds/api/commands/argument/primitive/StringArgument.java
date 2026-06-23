package net.survivalboom.sbds.api.commands.argument.primitive;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentAutoCompleteContext;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class StringArgument extends Argument<String> {

    private final Function<ArgumentAutoCompleteContext, List<Command.Choice>> autocomplete;

    public StringArgument(@Nullable Function<ArgumentAutoCompleteContext, List<Command.Choice>> autocomplete) {
        this.autocomplete = autocomplete;
    }

    public StringArgument() {
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
    public List<Command.Choice> onArgumentAutoComplete(@NotNull ArgumentAutoCompleteContext context) {
        return autocomplete.apply(context);
    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

    @Override
    public boolean isAutoComplete() {
        return autocomplete != null;
    }

}
