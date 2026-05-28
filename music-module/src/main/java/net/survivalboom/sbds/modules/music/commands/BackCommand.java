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

import java.util.List;

@CommandClass(name = "back", description = "Returns the previous song", translationKey = "music.command.back")
public class BackCommand extends AbstractPlayerCommand {

    public BackCommand(@NotNull MusicManager musicManager) {
        super(musicManager);
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) {
            return;
        }

        if (checkBannedOrLocked(info, player, false)) {
            return;
        }

        int steps = info.arguments().getCast("steps", Integer.class).orElse(1);
        int allowedSteps = player.getPlayingIndex();

        MusicTrack skippedTrack = player.getCurrentPlaying();

        try {
            player.changePlayingIndex(-steps);
        }

        catch (IllegalArgumentException e) {
            info.reply("music.command.skip.invalid-index")
                    .withPlaceholders("playlist.size", allowedSteps)
                    .send().setEphemeral(true)
                    .queue();
            return;
        }

        MusicTrack playingTrack = player.getCurrentPlaying();
        User botUser = player.getBot().getBot().getSelfUser();

        List<MusicTrack> playlist = player.getPlaylist();

        info.reply(steps == 1 ? "music.command.back.single" : "music.command.back.multiple")
                .withPlaceholders(
                        "bot", botUser,
                        "skipped", skippedTrack,
                        "playing", playingTrack,
                        "count", steps,
                        "playlist.size", playlist.size(),
                        "playlist", createTracksString(playlist, 10)
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

        int allowedSteps = player.getPlayingIndex();
        if (steps > allowedSteps) {
            info.logger().warn("Cannot go back {} tracks. Current index: {}", steps, allowedSteps);
            return;
        }

        MusicTrack skippedTrack = player.getCurrentPlaying();

        try {
            player.changePlayingIndex(-steps);
        } catch (IllegalArgumentException e) {
            info.logger().warn("Invalid back index. Current index: {}", player.getPlayingIndex());
            return;
        }

        MusicTrack playingTrack = player.getCurrentPlaying();

        info.logger().info("Went back {} track(s): {} -> {}", steps, skippedTrack.getTitle(), playingTrack.getTitle());

    }

    @ArgumentMethod(description = "Songs to skip", required = false, index = 1)
    public Argument<?> steps() {
        return new IntegerArgument();
    }

    @ArgumentMethod(description = "Channel with music bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
