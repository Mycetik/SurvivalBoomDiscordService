package net.survivalboom.sbds.api.commands.argument.misc.select;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractSelectArgument<T> extends Argument<T> {

    private final List<T> choices;

    public AbstractSelectArgument(@NotNull List<T> choices) {
        this.choices = choices;
    }

    @SafeVarargs
    public AbstractSelectArgument(@NotNull T... choices) {
        this.choices = List.of(choices);
    }

    // PARSING //

    @Override
    public @NotNull T parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        if (input instanceof OptionMapping mapping) {
            return choices.get(mapping.getAsInt());
        }

        throw new ArgumentParseException("Invalid value type `" + input + "`");

    }

    // DISCORD //

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.INTEGER;
    }

    @Override
    public @NotNull OptionData createOptionData(@NotNull CommandArgument argument) {

        OptionData optionData = Argument.createOptionData(argument, OptionType.INTEGER, false);
        for (int i = 0; i < choices.size(); i++) {
            optionData.addChoice(choices.get(i).toString(), i);
        }

        return optionData;

    }

}
