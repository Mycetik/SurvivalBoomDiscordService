package net.survivalboom.sbds.api.commands.argument;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;

public abstract class Argument<T> {

    public @NotNull T parse(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        try {
            return parse0(input, resources);
        }

        catch (ArgumentParseException e) {
            throw e;
        }

        catch (Throwable t) {
            throw new ArgumentParseException(t.getMessage(), t);
        }

    }

    protected abstract @NotNull T parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException;

    public abstract @NotNull OptionData build(@NotNull CommandArgument argument);


    public  int split(@NotNull String input) {

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);
            if (!Character.isSpaceChar(c)) continue;

            return i;

        }

        return input.length();

    }


    public record ArgumentResources(@NotNull ISBDS sbds, @NotNull TypeMap map) {}

}
