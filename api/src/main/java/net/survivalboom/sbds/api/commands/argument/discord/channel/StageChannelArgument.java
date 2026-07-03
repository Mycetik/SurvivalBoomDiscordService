package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;

public class StageChannelArgument extends ChannelArgument<StageChannel> {

    public StageChannelArgument() {
        super(StageChannel.class);
    }

}
