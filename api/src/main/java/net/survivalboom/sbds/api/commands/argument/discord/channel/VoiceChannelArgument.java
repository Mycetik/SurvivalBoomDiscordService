package net.survivalboom.sbds.api.commands.argument.discord.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.NotNull;

public class VoiceChannelArgument extends ChannelArgument<VoiceChannel> {

    public VoiceChannelArgument(@NotNull JDA bot) {
        super(VoiceChannel.class, bot);
    }

    public VoiceChannelArgument() {
        super(VoiceChannel.class);
    }

}
