package net.survivalboom.sbds.modules.music;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.commands.*;
import net.survivalboom.sbds.modules.music.lavalink.AutoSetup;

import java.util.ArrayList;
import java.util.List;
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

        List<CommandBase> commands = prepareCommands();
        commands.forEach(this::registerSlashCommand);

        Command command = Command.create("music", getModule());
        commands.forEach(cmd -> command.withSubcommand(cmd, getSbds(), getModule()));

        getSbds().getConsoleListener().registerCommand(this, command);

    }

    private List<CommandBase> prepareCommands() {

        List<CommandBase> list = new ArrayList<>();

        list.add(new PlayCommand(botManager));
        list.add(new StopCommand(botManager));
        list.add(new SkipCommand(botManager));
        list.add(new PlaylistCommand(botManager));

        list.add(new LoopCommand(botManager));
        list.add(new PauseCommand(botManager));

        list.add(new Music247Command(botManager));
        list.add(new LockCommand(botManager));
        list.add(new MusicBanCommand(botManager));

        return list;

    }




}
