package net.survivalboom.sbds.api.commands.argument;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.survivalboom.sbds.api.commands.CommandArgument;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Argument<T> {

    //
    // EXECUTION
    //

    @ApiStatus.OverrideOnly
    @ApiStatus.Internal
    public void onCommandExecute(
            @NotNull ArgumentExecutionContext<T> context
    ) {}

    //
    // PARSING
    //

    public abstract @NotNull T parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException;

    //
    // STRING
    //

    public int split(@NotNull String input) {

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);
            if (!Character.isSpaceChar(c)) {
                continue;
            }

            return i;

        }

        return input.length();

    }

    //
    // SLASH
    //

    public abstract @NotNull OptionType getOptionType();

    public @NotNull OptionData createOptionData(@NotNull CommandArgument argument) {
        return createOptionData(getOptionType(), argument);
    }

    public static @NotNull OptionData createOptionData(@NotNull OptionType type, @NotNull CommandArgument argument) {
        return new OptionData(type, argument.name(), Objects.requireNonNullElse(argument.description(), "-"), argument.required());
    }

}
