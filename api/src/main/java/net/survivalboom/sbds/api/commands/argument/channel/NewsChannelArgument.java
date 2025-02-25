package net.survivalboom.sbds.api.commands.argument.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import org.jetbrains.annotations.NotNull;

public class NewsChannelArgument extends ChannelArgument<NewsChannel> {

    public NewsChannelArgument(@NotNull JDA bot) {
        super(NewsChannel.class, bot);
    }

    public NewsChannelArgument() {
        super(NewsChannel.class);
    }

}
