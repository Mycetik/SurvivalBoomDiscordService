package net.survivalboom.sbds.modules.music.commands;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;

@Command(name = "stop")
public class StopCommand extends AbstractPlayerCommand {

    public StopCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false);
        if (player == null) return;

        player.stop();

        info.reply("music.command.stop").withPlaceholders(Placeholders.of("{BOT}", player.getBot().getBot().getSelfUser().getAsMention())).queue();

    }

}
