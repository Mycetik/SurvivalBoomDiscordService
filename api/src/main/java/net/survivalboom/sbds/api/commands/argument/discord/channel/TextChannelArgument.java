package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class TextChannelArgument extends ChannelArgument<TextChannel> {

    public TextChannelArgument() {
        super(TextChannel.class);
    }

}
