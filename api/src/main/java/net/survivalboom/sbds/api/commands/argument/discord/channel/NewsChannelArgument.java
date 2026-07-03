package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;

public class NewsChannelArgument extends ChannelArgument<NewsChannel> {

    public NewsChannelArgument() {
        super(NewsChannel.class);
    }

}
