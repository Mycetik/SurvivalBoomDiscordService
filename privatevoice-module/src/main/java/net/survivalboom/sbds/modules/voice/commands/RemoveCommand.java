package net.survivalboom.sbds.modules.voice.commands;

import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.voice.storage.VoiceCreatorChannels;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@CommandClass(name = "remove", description = "Removes and disables private channels from this guild.", translationKey = "voice.command.setup.remove", permission = "voice.command.remove")
public class RemoveCommand extends CommandBase implements SlashCommandExecutor {

    private final VoiceCreatorChannels voiceCreatorChannels;


    public RemoveCommand(@NotNull VoiceCreatorChannels voiceCreatorChannels) {
        this.voiceCreatorChannels = voiceCreatorChannels;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        voiceCreatorChannels.removeVoiceCreator(Objects.requireNonNull(info.guild()));
        info.reply("voice.command.setup.remove").queue();
    }

}
