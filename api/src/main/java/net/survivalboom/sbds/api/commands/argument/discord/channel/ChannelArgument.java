package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import org.jetbrains.annotations.NotNull;

public abstract class ChannelArgument<T extends Channel> extends SimpleArgument<T> {

    private final JDA bot;

    private final Class<T> tClass;

    public ChannelArgument(@NotNull Class<T> tClass, @NotNull JDA bot) {
        this.bot = bot;
        this.tClass = tClass;
    }

    public ChannelArgument(@NotNull Class<T> tclass) {
        this.tClass = tclass;
        this.bot = null;
    }

    protected @NotNull T getChannel(@NotNull String input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        T out = getBot(resources).getChannelById(tClass, input);
        if (out == null) throw new ArgumentParseException("Invalid channel");

        return out;

    }

    protected @NotNull JDA getBot(@NotNull ArgumentResources resources) {
        if (bot != null) return bot;
        return resources.sbds().getBot();
    }


    @Override
    public @NotNull T parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        if (input instanceof String string) {
            return getChannel(string, resources);
        }

        else if (input instanceof OptionMapping mapping) {
            return (T) mapping.getAsChannel();
        }

        throw new ArgumentParseException();


    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.CHANNEL;
    }

}
