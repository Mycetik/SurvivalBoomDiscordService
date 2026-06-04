package net.survivalboom.sbds.modules.music;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.commands.*;
import net.survivalboom.sbds.modules.music.music.lavalink.AutoSetup;

import java.util.List;
import java.util.stream.Stream;

public class MusicModule extends ModuleMain {

    private MusicManager musicManager;

    private AutoSetup autoSetup;

    @Override
    public void onLoad() {
        autoSetup = new AutoSetup(this);
        musicManager = new MusicManager(this, autoSetup);
    }

    @Override
    public void onEnable() {

        checkAndLoadConfig();

        addModuleTranslations(
                "translation_uk.yml",
                "translation_ru.yml",
                "translation_en.yml"
        );

        checkFiles2();

        List<Command> commands = prepareCommands();
        commands.forEach(this::registerSlashCommand);

        Command consoleCommand = Command.create("music")
                .setDescription("Manage MusicModule")
                .addSubCommand(commands)
                .build();

        getSbds().getConsoleListener().registerCommand(this, consoleCommand);

        autoSetup.init();
        musicManager.init();

    }

    @Override
    public void onDisable() {
        musicManager.shutdown();
        autoSetup.shutdown();
    }

    private List<Command> prepareCommands() {

        return Stream.of(
                new PlayCommand(musicManager),
                new StopCommand(musicManager),
                new SkipCommand(musicManager),
                new BackCommand(musicManager),
                new PlaylistCommand(musicManager),
                new LoopCommand(musicManager),
                new PauseCommand(musicManager),
                new Music247Command(musicManager),
                new LockCommand(musicManager),
                new MusicBanCommand(musicManager)
        ).map(CommandBase::build).toList();

    }




}
