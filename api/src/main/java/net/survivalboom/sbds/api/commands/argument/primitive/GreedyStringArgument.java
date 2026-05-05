package net.survivalboom.sbds.api.commands.argument.primitive;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;

public class GreedyStringArgument extends Argument<String> {

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
    public int split(@NotNull String input) {
        return input.length();
    }

}
