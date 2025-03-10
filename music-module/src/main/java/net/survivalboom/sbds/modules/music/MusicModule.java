package net.survivalboom.sbds.modules.music;

import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.commands.PlayCommand;
import net.survivalboom.sbds.modules.music.commands.PlaylistCommand;
import net.survivalboom.sbds.modules.music.commands.SkipCommand;
import net.survivalboom.sbds.modules.music.commands.StopCommand;
import org.slf4j.Logger;

import java.util.Map;

public class MusicModule extends ModuleMain {

    private BotManager botManager;

    private Logger logger;

    @Override
    public void onLoad() {
        logger = getModule().getLogger();
        botManager = new BotManager(this);
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        Map<String, String> map = Map.of(
                "translations/ukrainian.yml", "translations/ukrainian.yml",
                "translations/russian.yml", "translations/russian.yml",
                "translations/english.yml", "translations/english.yml"
        );
        checkFiles(map);

        getSbds().getTranslationManager().addModuleTranslations(this);

        botManager.init();

        ISlashCommandManager commandManager = getSbds().getSlashCommandManager();
        commandManager.registerCommand(this, new PlayCommand(botManager));
        commandManager.registerCommand(this, new StopCommand(botManager));
        commandManager.registerCommand(this, new SkipCommand(botManager));
        commandManager.registerCommand(this, new PlaylistCommand());

    }

    @Override
    public void onDisable() {
        botManager.shutdown();
    }

}
