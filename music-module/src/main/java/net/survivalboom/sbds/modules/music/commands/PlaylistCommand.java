package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.MusicTrack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@CommandClass(name = "playlist", description = "Shows current playlist", translationKey = "music.command.playlist")
public class PlaylistCommand extends AbstractPlayerCommand {

    public PlaylistCommand(@NotNull MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) {
            return;
        }

        List<MusicTrack> playlist = player.getPlaylist();
        String playListStr = createTracksString(playlist, 100);
        User botUser = player.getBot().getBot().getSelfUser();

        info.reply("music.command.playlist.success")
                .withPlaceholders(
                        "bot", botUser,
                        "playlist", playListStr,
                        "playlist.size", playlist.size()
                )
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();
        Objects.requireNonNull(channel);

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().error("No music player found for the specified channel.");
            return;
        }

        if (player.getPlaylistSize() == 0) {
            info.logger().info("The playlist is currently empty.");
            return;
        }

        List<MusicTrack> playlist = player.getPlaylist();

        info.logger().info("--- --- < Playlist > --- ---");

        for (int i = 0; i < playlist.size(); i++) {
            MusicTrack track = playlist.get(i);
            info.logger().info("{}. {} {} -> {}", i, track.getDurationFormated(), track.getTitle(), track.getLink());
        }

        info.logger().info("--- --- -----  ----- --- ---");

    }

    @ArgumentMethod(description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
