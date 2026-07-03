package net.survivalboom.sbds.api.commands.argument;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.CommandArgument;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class Argument<T> {

    //
    // EXECUTION
    //

    @ApiStatus.OverrideOnly
    public void onCommandExecute(
            @NotNull ArgumentExecutionContext<T> context
    ) {}

    @ApiStatus.OverrideOnly
    public @Nullable List<Command.Choice> onArgumentAutoComplete(@NotNull ArgumentAutoCompleteContext context) {
        return List.of(
                new Command.Choice("ua.timurishche.DinosaurDeathException", 1),
                new Command.Choice("java.lang.OutOfMemoryError", 2),
                new Command.Choice("RAWR-R-R!!!!", 3)
        );
    }

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

    public boolean isAutoComplete() {
        return false;
    }


    public @NotNull OptionData createOptionData(@NotNull CommandArgument argument) {
        return createOptionData(argument, getOptionType(), isAutoComplete());
    }

    public static @NotNull OptionData createOptionData(@NotNull CommandArgument argument, @NotNull OptionType type, boolean autocomplete) {
        return new OptionData(
                type,
                argument.name(),
                Objects.requireNonNullElse(argument.description(), "-"),
                argument.required(),
                autocomplete
        );
    }

}
