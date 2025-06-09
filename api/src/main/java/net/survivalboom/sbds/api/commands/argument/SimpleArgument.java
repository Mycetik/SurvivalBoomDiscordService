package net.survivalboom.sbds.api.commands.argument;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.survivalboom.sbds.api.commands.CommandArgument;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class SimpleArgument<T> extends Argument<T> {

    public abstract @NotNull OptionType getOptionType();

    @Override
    public @NotNull OptionData build(@NotNull CommandArgument argument) {
        return createOptionData(getOptionType(), argument);
    }


    public static @NotNull OptionData createOptionData(@NotNull OptionType type, @NotNull CommandArgument argument) {
        return new OptionData(type, argument.name(), Objects.requireNonNullElse(argument.description(), "Option has no description."), argument.required());
    }

}
