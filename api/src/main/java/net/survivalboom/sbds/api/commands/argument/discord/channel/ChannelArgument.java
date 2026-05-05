package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;

public abstract class ChannelArgument<T extends Channel> extends Argument<T> {

    private final Class<T> clazz;

    public ChannelArgument(@NotNull Class<T> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull T parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        if (input instanceof String string) {

            T channel = context.sbds().getBot().getChannelById(clazz, string);
            if (channel == null) {
                throw new ArgumentParseException("Invalid channel `" + string + "`");
            }

            return channel;

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
