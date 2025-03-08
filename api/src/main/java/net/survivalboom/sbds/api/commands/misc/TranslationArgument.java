package net.survivalboom.sbds.api.commands.misc;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentResources;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import net.survivalboom.sbds.api.translations.ITranslation;
import org.jetbrains.annotations.NotNull;

public class TranslationArgument extends SimpleArgument<ITranslation> {


    @NotNull
    @Override
    protected ITranslation parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        String translationName;
        if (input instanceof String s) {
            translationName = s;
        }

        else if (input instanceof OptionMapping optionMapping) {
            translationName = optionMapping.getAsString();
        }

        else throw new ArgumentParseException("Invalid type `" + input.getClass().getName() + "`");

        ITranslation translation = resources.sbds().getTranslationManager().getTranslation(translationName);
        if (translation == null) throw new ArgumentParseException("Invalid translation `" + translationName + "`");
        return translation;

    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return OptionType.STRING;
    }

}
