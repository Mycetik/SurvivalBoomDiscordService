package net.survivalboom.sbds.api.commands.argument.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;

public class GuildArgument extends Argument<Guild> {

    @Override
    public @NotNull Guild parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        String guildID;
        if (input instanceof String string) {
            guildID = string;
        }

        else if (input instanceof OptionMapping option) {
            guildID = option.getAsString();
        }

        else {
            throw new ArgumentParseException();
        }

        Guild guild = context.sbds().getBot().getGuildById(guildID);
        if (guild == null) {
            throw new ArgumentParseException("Guild with id `" + guildID + "` not found");
        }

        return guild;

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

}
