package net.survivalboom.sbds.api.commands.argument.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import org.jetbrains.annotations.NotNull;

public class UserArgument extends SimpleArgument<User> {

    @NotNull
    @Override
    protected User parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        JDA bot = resources.sbds().getBot();

        if (input instanceof String string) {
            User user = bot.getUserById(string);
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
