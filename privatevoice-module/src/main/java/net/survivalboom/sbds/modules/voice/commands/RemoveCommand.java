package net.survivalboom.sbds.modules.voice.commands;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.voice.storage.VoiceCreatorChannels;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "remove", description = "Removes and disables private channels from this guild.", permission = "voice.command.remove")
public class RemoveCommand extends CommandBase implements SlashCommand {

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
