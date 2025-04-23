package net.survivalboom.sbds.modules.privatevoice.commands;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.privatevoice.VoiceManager;
import org.jetbrains.annotations.NotNull;

@Command(name = "private-voice-setup", description = "ХУЙ")
public class SetupCommand extends CommandBase implements SlashCommand {

    private final VoiceManager voiceManager;

    public SetupCommand(VoiceManager voiceManager) {
        this.voiceManager = voiceManager;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        VoiceChannel channel = info.arguments().getCastOrNull("channel", VoiceChannel.class);

        assert channel != null;
        voiceManager.setup(channel);

        info.reply("commands.private-voice.successful-setup").queue();
    }

    @CommandArgument(name = "channel")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }
}
