package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

public class TextChannelArgument extends ChannelArgument<TextChannel> {

    public TextChannelArgument(@NotNull JDA bot) {
        super(TextChannel.class, bot);
    }

    public TextChannelArgument() {
        super(TextChannel.class);
    }

}
