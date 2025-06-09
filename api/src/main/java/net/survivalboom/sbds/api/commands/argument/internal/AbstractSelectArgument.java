package net.survivalboom.sbds.api.commands.argument.internal;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
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


    @Override
    protected @NotNull T parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        if (input instanceof OptionMapping mapping) {
            return choices.get(mapping.getAsInt());
        }

        throw new ArgumentParseException("Invalid value type `" + input + "`");

    }

    @Override
    public @NotNull OptionData build(@NotNull CommandArgument argument) {
        OptionData optionData = SimpleArgument.createOptionData(OptionType.INTEGER, argument);
        for (int i = 0; i < choices.size(); i++) optionData.addChoice(choices.get(i).toString(), i);
        return optionData;
    }

}
