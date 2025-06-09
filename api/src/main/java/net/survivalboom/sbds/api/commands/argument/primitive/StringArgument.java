package net.survivalboom.sbds.api.commands.argument.primitive;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import org.jetbrains.annotations.NotNull;

public class StringArgument extends SimpleArgument<String> {

    @Override
    public @NotNull String parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

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


}
