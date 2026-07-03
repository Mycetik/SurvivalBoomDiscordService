package net.survivalboom.sbds.api.commands.argument.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;

public class UserArgument extends Argument<User> {

    @Override
    public @NotNull User parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        JDA bot = context.sbds().getBot();

        if (input instanceof String string) {
            User user = bot.retrieveUserById(string).complete();
            if (user == null) throw new ArgumentParseException("User with id `" + string + "` not found");
            return user;
        }

        if (input instanceof OptionMapping optionMapping) {
            return optionMapping.getAsUser();
        }

        throw new ArgumentParseException();

    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return OptionType.USER;
    }

}
