package net.survivalboom.sbds.api.commands.argument.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.channel.Channel;

public class MentionableArgument extends Argument<IMentionable> {

    @Override
    public @NotNull IMentionable parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        if (input instanceof String string) {

            IMentionable mentionable = getMentionable(context, string);
            if (mentionable == null) {
                throw new ArgumentParseException("Unknown mentionable with id `" + string + "`");
            }

            return mentionable;

        }

        else if (input instanceof OptionMapping optionMapping) {
            return optionMapping.getAsMentionable();
        }

        throw new ArgumentParseException();

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.MENTIONABLE;
    }


    private @Nullable IMentionable getMentionable(@NotNull ArgumentParsingContext context, @NotNull String string) {

        JDA bot = context.sbds().getBot();

        IMentionable mentionable = bot.getRoleById(string);
        if (mentionable != null) {
            return mentionable;
        }

        mentionable = bot.getChannelById(Channel.class, string);
        if (mentionable != null) {
            return mentionable;
        }

        mentionable = bot.getUserById(string);

        return mentionable;

    }

}
