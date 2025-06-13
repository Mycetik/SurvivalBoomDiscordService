package net.survivalboom.sbds.modules.voice.commands;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.voice.storage.VoiceCreatorChannels;
import org.jetbrains.annotations.NotNull;

@Command(name = "set", description = "Sets a voice for creator in this guild.", permission = "voice.command.setup")
public class SetupCommand extends CommandBase implements SlashCommand {

    private final VoiceCreatorChannels voiceCreatorChannels;

    public SetupCommand(@NotNull VoiceCreatorChannels voiceCreatorChannels) {
        this.voiceCreatorChannels = voiceCreatorChannels;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        VoiceChannel channel = info.arguments().getCastOrNull("channel", VoiceChannel.class);
        if (channel == null) {
            info.reply("voice.command.setup.invalid-channel").queue();
            return;
        }

        voiceCreatorChannels.setVoiceCreator(channel);

        info.reply("voice.command.setup.set").withPlaceholders("{CHANNEL}", channel.getAsMention()).queue();

    }

    @CommandArgument(name = "channel")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
