package net.survivalboom.sbds.modules.music;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.commands.*;
import net.survivalboom.sbds.modules.music.commands.console.*;
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

        addModuleTranslations();

        registerSlashCommand(new PlayCommand(botManager));
        registerSlashCommand( new StopCommand(botManager));
        registerSlashCommand(new SkipCommand(botManager));
        registerSlashCommand(new BackCommand(botManager));
        registerSlashCommand(new PlaylistCommand(botManager));

        registerSlashCommand(new LoopCommand(botManager));
        registerSlashCommand(new PauseCommand(botManager));

        registerSlashCommand(new Music247Command(botManager));
        registerSlashCommand(new LockCommand(botManager));
        registerSlashCommand(new MusicBanCommand(botManager));

        registerConsoleCommand(new ConsolePlayCommand(botManager));
        registerConsoleCommand(new ConsoleStopCommand(botManager));
        registerConsoleCommand(new ConsoleSkipCommand(botManager));
        registerConsoleCommand(new ConsoleBackCommand(botManager));
        registerConsoleCommand(new ConsolePlaylistCommand(botManager));

        registerConsoleCommand(new ConsoleLoopCommand(botManager));
        registerConsoleCommand(new ConsolePauseCommand(botManager));

        registerConsoleCommand(new ConsoleMusic247Command(botManager));
        registerConsoleCommand(new ConsoleLockCommand(botManager));
        registerConsoleCommand(new ConsoleMusicBanCommand(botManager));

    }

}
