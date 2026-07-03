package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.commands.string.StringCommandExecutor;
import net.survivalboom.sbds.api.commands.string.StringExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.modules.music.MusicModule;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.MusicTrack;
import net.survivalboom.sbds.modules.music.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CommandClass(
        name = "playlist",
        description = "Shows current playlist",
        translationKey = "music.command.playlist",
        permission = "music.command.playlist",
        defaultPermission = true
)
public class PlaylistCommand extends CommandBase implements SlashCommandExecutor, StringCommandExecutor, ConsoleCommandExecutor {

    private final MusicManager manager;

    public PlaylistCommand(@NotNull MusicModule module) {
        this.manager = module.getMusicManager();
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        executes0(info);
    }

    @Override
    public void executes(@NotNull StringExecutionInfo info) {
        executes0(info);
    }

    private void executes0(@NotNull InteractionHolder info) {

        GuildPlayer player = Utils.getInteractionPlayer(manager, info, false, false);
        if (player == null) {
            return;
        }

        List<MusicTrack> playlist = player.getPlaylist();
        String playListStr = Utils.createTracksString(playlist, 30);
        User botUser = player.getBot().getBot().getSelfUser();

        int index = player.getPlayingIndex() + 1;

        info.reply("music.command.playlist.success")
                .withPlaceholders(
                        "bot", botUser,
                        "playing", player.getCurrentPlaying(),
                        "playing.index", index,
                        "playlist", playListStr,
                        "playlist.size", playlist.size()
                )
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();

        GuildPlayer player = Utils.getConsolePlayer(manager, info, channel, false);
        if (player == null) {
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
