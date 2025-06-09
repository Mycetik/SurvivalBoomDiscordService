package net.survivalboom.sbds.modules.music;

import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.commands.*;
import net.survivalboom.sbds.modules.music.lavalink.AutoSetup;

import java.util.Map;

public class MusicModule extends ModuleMain {

    private BotManager botManager;

    private AutoSetup autoSetup;

    @Override
    public void onLoad() {
        autoSetup = new AutoSetup(this);
        botManager = new BotManager(this, autoSetup);
    }

    @Override
    public void onEnable() {

        registerModuleComponents();

        autoSetup.init();
        botManager.init();

    }

    @Override
    public void onDisable() {
        botManager.shutdown();
        autoSetup.shutdown();
    }


    private void registerModuleComponents() {

        saveDefaultConfig();

        Map<String, String> map = Map.of(
                "translations/translation_uk.yml", "translations/translation_uk.yml",
                "translations/translation_ru.yml", "translations/translation_ru.yml",
                "translations/translation_en.yml", "translations/translation_en.yml"
        );
        checkFiles(map);

        getSbds().getTranslationManager().addModuleTranslations(this);

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

}
