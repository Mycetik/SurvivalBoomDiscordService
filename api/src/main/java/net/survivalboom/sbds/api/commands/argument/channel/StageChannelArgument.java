package net.survivalboom.sbds.api.commands.argument.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import org.jetbrains.annotations.NotNull;

public class StageChannelArgument extends ChannelArgument<StageChannel> {

    public StageChannelArgument(@NotNull JDA bot) {
        super(StageChannel.class, bot);
    }

    public StageChannelArgument() {
        super(StageChannel.class);
    }

}
