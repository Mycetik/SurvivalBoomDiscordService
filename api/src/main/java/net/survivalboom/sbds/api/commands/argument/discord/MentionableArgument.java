package net.survivalboom.sbds.api.commands.argument.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.channel.Channel;

public class MentionableArgument extends SimpleArgument<IMentionable> {

    @NotNull
    @Override
    protected IMentionable parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        if (input instanceof String string) {

            IMentionable mentionable = getMentionable(resources, string);
            if (mentionable == null) throw new ArgumentParseException("Unknown mentionable with id `" + string + "`");

            return mentionable;

        }

        if (input instanceof OptionMapping optionMapping) {
            return optionMapping.getAsMentionable();
        }

        return null;
    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return OptionType.MENTIONABLE;
    }


    private @Nullable IMentionable getMentionable(@NotNull ArgumentResources resources, @NotNull String string) {

        JDA bot = resources.sbds().getBot();

        IMentionable mentionable = getRole(bot, string);
        if (mentionable != null) return mentionable;

        mentionable = getChannel(bot, string);
        if (mentionable != null) return mentionable;

        mentionable = getUser(bot, string);

        return mentionable;

    }

    private @Nullable User getUser(@NotNull JDA bot, @NotNull String string) {
        return bot.getUserById(string);
    }

    private @Nullable Channel getChannel(@NotNull JDA bot, @NotNull String string) {
        return bot.getChannelById(Channel.class, string);
    }

    private @Nullable Role getRole(@NotNull JDA bot, @NotNull String string) {
        return bot.getRoleById(string);
    }

}
