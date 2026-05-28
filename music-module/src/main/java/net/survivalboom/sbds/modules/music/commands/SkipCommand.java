package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.MusicTrack;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "skip", description = "Skips the current playing song", translationKey = "music.command.skip")
public class SkipCommand extends AbstractPlayerCommand {

    public SkipCommand(@NotNull MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) return;

        if (checkBannedOrLocked(info, player, false)) return;

        if (player.isLastTrack()) {
            player.shutdown();
            info.reply("music.command.stop.success").queue();
            return;
        }

        int steps = info.arguments().getCast("steps", Integer.class).orElse(1);


        MusicTrack skippedTrack = player.getCurrentPlaying();

        try {
            player.changePlayingIndex(steps);
        }

        catch (IllegalArgumentException e) {
            int allowedSteps = player.getPlaylistSize() - player.getPlayingIndex();
            info.reply("music.command.skip.invalid-index")
                    .withPlaceholders("{playlist.size}", allowedSteps)
                    .queue();
            return;
        }

        MusicTrack playingTrack = player.getCurrentPlaying();
        User botUser = player.getBot().getBot().getSelfUser();

        info.reply(steps == 1 ? "music.command.skip.single" : "music.command.skip.multiple")
                .withPlaceholders(
                        "bot", botUser,
                        "skipped", skippedTrack,
                        "playing", playingTrack,
                        "count", steps,
                        "playlist", createTracksString(player.getPlaylist(), 10),
                        "playlist.size", player.getPlaylistSize()
                )
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();
        int steps = info.arguments().getCast("steps", Integer.class).orElse(1);

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().warn("No player found for the given channel.");
            return;
        }

        if (player.isLastTrack()) {
            player.shutdown();
            info.logger().info("Last track reached. Stopping player.");
            return;
        }

        MusicTrack skippedTrack = player.getCurrentPlaying();

        try {
            player.changePlayingIndex(steps);
        } catch (IllegalArgumentException e) {
            info.logger().warn("Invalid skip index. Playlist size: {}", player.getPlaylistSize());
            return;
        }

        MusicTrack playingTrack = player.getCurrentPlaying();

        info.logger().info("Skipped {} song(s): {} -> {}", steps, skippedTrack.getTitle(), playingTrack.getTitle());

    }

    @ArgumentMethod(description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

    @ArgumentMethod(description = "Songs to skip", required = false)
    public Argument<?> steps() {
        return new IntegerArgument();
    }

}
