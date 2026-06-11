package net.survivalboom.sbds.api.commands.argument.primitive;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;

public class StringToObjectArgument extends Argument<Object> {

    @Override
    public @NotNull Object parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        String string;
        if (input instanceof String s) {
            string = s;
        }

        else if (input instanceof OptionMapping mapping) {
            string = mapping.getAsString();
        }

        else {
            throw new ArgumentParseException("Invalid object `" + input + "`");
        }

        try {
            return Integer.parseInt(string);
        }

        catch (NumberFormatException ignored) {}

        if (string.equals("true") || string.equals("false")) {
            return string.equals("true");
        }

        return string;

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

}
