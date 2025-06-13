package net.survivalboom.sbds.modules.music.commands.console;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import org.jetbrains.annotations.NotNull;

@Command(name = "music")
public class ConsoleMusicCommand extends CommandBase implements ConsoleCommand {
    public ConsoleMusicCommand(@NotNull BotManager botManager) {
        addSubCommand(new ConsolePlayCommand(botManager));
        addSubCommand(new ConsoleStopCommand(botManager));
        addSubCommand(new ConsoleSkipCommand(botManager));
        addSubCommand(new ConsoleBackCommand(botManager));
        addSubCommand(new ConsolePlaylistCommand(botManager));

        addSubCommand(new ConsoleLoopCommand(botManager));
        addSubCommand(new ConsolePauseCommand(botManager));

        addSubCommand(new ConsoleMusic247Command(botManager));
        addSubCommand(new ConsoleLockCommand(botManager));
        addSubCommand(new ConsoleMusicBanCommand(botManager));

    }
}
