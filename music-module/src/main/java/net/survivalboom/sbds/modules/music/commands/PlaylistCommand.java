package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;

@Command(name = "playlist")
public class PlaylistCommand extends AbstractPlayerCommand {

    public PlaylistCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) {
            return;
        }

        String playListStr = createTracksString(player.getPlaylist(), true, 100);
        User botUser = player.getBot().getBot().getSelfUser();

        Placeholders placeholders = new Placeholders();
        placeholders
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl())
                .add("{COUNT}", player.getPlaylistSize())
                .add("{PLAYLIST}", playListStr);

        info.reply("music.command.playlist").withPlaceholders(placeholders).queue();

    }

}
