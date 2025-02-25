package net.survivalboom.sbds.api.commands.argument.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import org.jetbrains.annotations.NotNull;

public class PrivateChannelArgument extends ChannelArgument<PrivateChannel> {

    public PrivateChannelArgument(@NotNull JDA bot) {
        super(PrivateChannel.class, bot);
    }

    public PrivateChannelArgument() {
        super(PrivateChannel.class);
    }

}
