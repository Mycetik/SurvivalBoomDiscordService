package net.survivalboom.sbds.api.commands.argument.primitive;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;

public class IntegerArgument extends Argument<Integer> {

    @Override
    public @NotNull Integer parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        if (input instanceof String string) {

            try {
                return Integer.parseInt(string);
            }

            catch (NumberFormatException e) {
                throw new ArgumentParseException("invalid integer");
            }

        }

        else if (input instanceof OptionMapping mapping) {
            return mapping.getAsInt();
        }

        throw new ArgumentParseException();

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.INTEGER;
    }

}
