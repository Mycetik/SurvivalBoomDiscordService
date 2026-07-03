package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class GuildChannelArgument extends ChannelArgument<GuildChannel> {

    public GuildChannelArgument() {
        super(GuildChannel.class);
    }

}
