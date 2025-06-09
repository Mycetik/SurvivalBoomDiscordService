package net.survivalboom.sbds.modules.music;

import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.commands.*;

import java.util.Map;

public class MusicModule extends ModuleMain {

    private BotManager botManager;

    @Override
    public void onLoad() {
        botManager = new BotManager(this);
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        Map<String, String> map = Map.of(
                "translations/translation_uk.yml", "translations/translation_uk.yml",
                "translations/translation_ru.yml", "translations/translation_ru.yml",
                "translations/translation_en.yml", "translations/translation_en.yml"
        );
        checkFiles(map);

        getSbds().getTranslationManager().addModuleTranslations(this);

        botManager.init();

        ISlashCommandManager commandManager = getSbds().getSlashCommandManager();
        commandManager.registerCommand(this, new PlayCommand(botManager));
        commandManager.registerCommand(this, new StopCommand(botManager));
        commandManager.registerCommand(this, new SkipCommand(botManager));
        commandManager.registerCommand(this, new BackCommand(botManager));
        commandManager.registerCommand(this, new PlaylistCommand(botManager));

        commandManager.registerCommand(this, new LoopCommand(botManager));
        commandManager.registerCommand(this, new PauseCommand(botManager));

        commandManager.registerCommand(this, new Music247Command(botManager));
        commandManager.registerCommand(this, new LockCommand(botManager));
        commandManager.registerCommand(this, new MusicBanCommand(botManager));

    }

    @Override
    public void onDisable() {
        botManager.shutdown();
    }

}
