package net.survivalboom.sbds.modules.voice.commands;

import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.modules.voice.storage.VoiceCreatorChannels;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "voice")
public class VoiceCommand extends CommandBase {

    public VoiceCommand(@NotNull VoiceCreatorChannels voiceCreatorChannels) {
        addSubCommand(new SetupCommand(voiceCreatorChannels));
        addSubCommand(new RemoveCommand(voiceCreatorChannels));
    }

}
