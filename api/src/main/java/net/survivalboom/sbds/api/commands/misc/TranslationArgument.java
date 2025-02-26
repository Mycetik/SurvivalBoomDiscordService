package net.survivalboom.sbds.api.commands.misc;

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

        if (input instanceof String s) {
            ITranslation translation = resources.sbds().getTranslationManager().getTranslation(s);
            if (translation == null) throw new ArgumentParseException("Invalid translation `" + s + "`");
            return translation;
        }

        throw new ArgumentParseException("Invalid type");

    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return OptionType.STRING;
    }

}
