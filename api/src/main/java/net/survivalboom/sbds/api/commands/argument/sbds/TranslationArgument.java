package net.survivalboom.sbds.api.commands.argument.sbds;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.translations.ITranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TranslationArgument extends Argument<ITranslation> {

    @Override
    public @NotNull ITranslation parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        String translationName;
        if (input instanceof String s) {
            translationName = s;
        }

        else if (input instanceof OptionMapping optionMapping) {
            translationName = optionMapping.getAsString();
        }

        else throw new ArgumentParseException("Invalid type `" + input.getClass().getName() + "`");

        ITranslation translation = context.sbds().getTranslationManager().getTranslation(translationName);
        if (translation == null) {
            throw new ArgumentParseException("Invalid translation `" + translationName + "`");
        }

        return translation;

    }

    @Override
    public @Nullable List<Command.Choice> onArgumentAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event, @NotNull ISBDS sbds) {
        return sbds.getTranslationManager().getTranslations()
                .stream()
                .map(translation -> new Command.Choice(translation.getDisplayName(), translation.getName()))
                .toList();
    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

    @Override
    public boolean isAutoComplete() {
        return true;
    }

}
