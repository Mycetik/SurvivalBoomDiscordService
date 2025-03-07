package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;

public class GuildChannelArgument extends ChannelArgument<GuildChannel> {

    public GuildChannelArgument(@NotNull JDA bot) {
        super(GuildChannel.class, bot);
    }

    public GuildChannelArgument() {
        super(GuildChannel.class);
    }

}
