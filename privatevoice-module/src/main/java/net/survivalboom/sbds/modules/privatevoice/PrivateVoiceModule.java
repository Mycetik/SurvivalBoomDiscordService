package net.survivalboom.sbds.modules.privatevoice;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.privatevoice.commands.SetupCommand;

import java.util.Map;

public class PrivateVoiceModule extends ModuleMain {

    private final VoiceManager voiceManager = new VoiceManager(this);

    @Override
    public void onEnable() {
        getLogger().info("Private Voice Module enabling");
        voiceManager.init0();

        checkFiles(Map.of("translations/translation_ru.yml", "translations/translation_ru.yml"));
        getSbds().getTranslationManager().addModuleTranslations(this);

        getSbds().getEventManager().registerEvents(this, voiceManager);
        getSbds().getSlashCommandManager().registerCommand(this, new SetupCommand(voiceManager));
    }

    @Override
    public void onDisable() {
        voiceManager.shutdown0();
        getLogger().info("Private voice module disabled");
    }


}