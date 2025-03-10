package net.survivalboom.sbds.modules.music.commands;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "skip")
public class SkipCommand extends AbstractPlayerCommand {

    public SkipCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info);
        if (player == null) return;

        if (!player.skip(1)) {
            info.reply("command.music-module.bot-stopped").queue();
            return;
        }

        Track track = Objects.requireNonNull(player.getCurrentPlaying());
        TrackInfo trackInfo = track.getInfo();

        Placeholders placeholders = new Placeholders();
        placeholders.add("{SKIPPED_SONG_NAME}", trackInfo.getSourceName());
        placeholders.add("{PLAYING_SONG_NAME}", trackInfo.getSourceName());
        placeholders.add("{PLAYING_SONG_DURATION}", trackInfo.getLength());
        placeholders.add("{BOT}", info.sbds().getBot().getSelfUser().getAsMention());

        info.reply("commands.music-module.song-skipped", placeholders).queue();

    }

}
