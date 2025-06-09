package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;

@Command(name = "music-24-7", description = "Disable idle disconnect for the current music bot", permission = "music.command.24_7")
public class Music247Command extends AbstractPlayerCommand {

    public Music247Command(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) throws Throwable {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) {
            return;
        }

        if (checkBannedOrLocked(info, player, false)) {
            return;
        }

        boolean state = !player.idleDisconnect();
        player.idleDisconnect(state);

        String str = state ? "music.command.24-7.disable" : "music.command.24-7.enable";

        User botUser = player.getBot().getBot().getSelfUser();
        Placeholders placeholders = new Placeholders()
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl());

        info.reply(str).withPlaceholders(placeholders).queue();

    }

}
