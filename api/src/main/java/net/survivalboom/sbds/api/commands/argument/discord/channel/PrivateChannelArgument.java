package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

public class PrivateChannelArgument extends ChannelArgument<PrivateChannel> {

    public PrivateChannelArgument() {
        super(PrivateChannel.class);
    }

}
