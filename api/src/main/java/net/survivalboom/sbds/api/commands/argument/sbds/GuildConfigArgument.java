package net.survivalboom.sbds.api.commands.argument.sbds;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentAutoCompleteContext;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuildConfigArgument extends Argument<IGuildConfigTemplate> {

    @Override
    public @NotNull IGuildConfigTemplate parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        String string;
        if (input instanceof String s) {
            string = s;
        }

        else if (input instanceof OptionMapping mapping) {
            string = mapping.getAsString();
        }

        else {
            throw new ArgumentParseException("Invalid obj `" + input + "`");
        }

        IGuildConfigTemplate template = context.sbds().getGuildConfigManager().getTemplate(string);
        if (template == null) {
            throw new ArgumentParseException("Guild config `" + string + "` does not exist");
        }

        return template;

    }

    @Override
    public @Nullable List<Command.Choice> onArgumentAutoComplete(@NotNull ArgumentAutoCompleteContext context) {
        return context.sbds().getGuildConfigManager().getTemplates()
                .stream()
                .map(template -> new Command.Choice(
                        template.createTranslationKey(),
                        template.getKey()
                ))
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
