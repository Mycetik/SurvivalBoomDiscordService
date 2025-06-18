package net.survivalboom.sbds.modules.voice;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.voice.commands.VoiceCommand;
import net.survivalboom.sbds.modules.voice.listener.ControlPanelListener;
import net.survivalboom.sbds.modules.voice.listener.GuildEventListener;
import net.survivalboom.sbds.modules.voice.storage.VoiceCreatorChannels;
import net.survivalboom.sbds.modules.voice.voice.VoiceManager;

import java.util.Map;

public class PrivateVoiceModule extends ModuleMain {

    private VoiceManager voiceManager;

    private VoiceCreatorChannels voiceCreatorChannels;

    private GuildEventListener guildEventListener;

    @Override
    public void onLoad() {
        voiceCreatorChannels = new VoiceCreatorChannels(this);
        voiceManager = new VoiceManager(this, voiceCreatorChannels);
        guildEventListener = new GuildEventListener(voiceManager);
    }

    @Override
    public void onEnable() {

        voiceCreatorChannels.init();
        voiceManager.init();

        checkFiles(Map.of(
                "translations/translation_uk.yml", "translations/translation_uk.yml",
                "translations/translation_ru.yml", "translations/translation_ru.yml",
                "translations/translation_en.yml", "translations/translation_en.yml"
        ));
        addModuleTranslations();

        registerEvents(guildEventListener);
        registerStringDropdown("action", new ControlPanelListener(voiceManager)::onControlPanelDropdown);
        registerSlashCommand(new VoiceCommand(voiceCreatorChannels));

    }

    @Override
    public void onDisable() {
        voiceManager.shutdown();
        voiceCreatorChannels.shutdown();
    }


}