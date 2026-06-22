package net.survivalboom.sbds.modules.music;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.commands.*;
import net.survivalboom.sbds.modules.music.utils.IntegratedLavalinkManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class MusicModule extends ModuleMain {

    private MusicManager musicManager;

    private IntegratedLavalinkManager integratedLavalinkManager;

    @Override
    public void onLoad() {
        integratedLavalinkManager = new IntegratedLavalinkManager(this);
        musicManager = new MusicManager(this, integratedLavalinkManager);
    }

    @Override
    public void onEnable() {

        checkAndLoadConfig();

        addModuleTranslations2(
                "translation_uk.yml",
                "translation_ru.yml",
                "translation_en.yml"
        );

        integratedLavalinkManager.init();
        musicManager.init();

        List<Command> commands = prepareCommands();
        commands.forEach(this::registerSlashCommand);
        commands.forEach(this::registerStringCommand);

        Command consoleCommand = Command.create("music")
                .setDescription("Manage MusicModule")
                .addSubCommand(commands)
                .build();

        registerConsoleCommand(consoleCommand);

    }

    @Override
    public void onDisable() {
        musicManager.shutdown();
        integratedLavalinkManager.shutdown();
    }

    private List<Command> prepareCommands() {

        return Stream.of(
                new PlayCommand(this),
                new StopCommand(this),
                new SkipCommand(this),
                new BackCommand(this),
                new PlaylistCommand(this),
                new LoopCommand(this),
                new PauseCommand(this),
                new Music247Command(this),
                new LockCommand(this),
                new MusicBanCommand(this)
        ).map(CommandBase::build).toList();

    }

    public @NotNull MusicManager getMusicManager() {
        return musicManager;
    }

    public @NotNull IntegratedLavalinkManager getIntegratedLavalinkManager() {
        return integratedLavalinkManager;
    }

}
