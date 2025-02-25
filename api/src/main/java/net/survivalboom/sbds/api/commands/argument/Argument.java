package net.survivalboom.sbds.api.commands.argument;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

public abstract class Argument<T> {

    public @NotNull T parse(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        try {
            return parse0(input, resources);
        }

        catch (Throwable t) {
            throw new ArgumentParseException(t.getMessage(), t);
        }

    }

    protected abstract @NotNull T parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException;

    public abstract @NotNull OptionType getOptionType();

    public abstract int split(@NotNull String input);


}
