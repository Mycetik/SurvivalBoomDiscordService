package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class VoiceChannelArgument extends ChannelArgument<VoiceChannel> {

    public VoiceChannelArgument() {
        super(VoiceChannel.class);
    }

}
