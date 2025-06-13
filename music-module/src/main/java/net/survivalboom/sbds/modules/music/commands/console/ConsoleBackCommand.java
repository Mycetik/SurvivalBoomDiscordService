package net.survivalboom.sbds.modules.music.commands.console;

import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.commands.AbstractPlayerCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Command(name = "back", description = "Go back N songs in the playlist")
public class ConsoleBackCommand extends AbstractPlayerCommand implements ConsoleCommand {

    public ConsoleBackCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
        int steps = info.arguments().getCastOrDefault("count", Integer.class, 1);

        if (channel == null || steps < 1) {
            info.logger().warn("Invalid channel or back count.");
            return;
        }

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

        TrackInfo skippedTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();

        try {
            player.changePlayingIndex(-steps);
        } catch (IllegalArgumentException e) {
            info.logger().warn("Invalid back index. Current index: {}", player.getPlayingIndex());
            return;
        }

        TrackInfo playingTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();

        info.logger().info("Went back {} track(s): {} -> {}", steps, skippedTrack.getTitle(), playingTrack.getTitle());
    }

    @CommandArgument(name = "channel", description = "The voice channel")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

    @CommandArgument(name = "count", description = "How many songs to go back", required = false)
    public Argument<?> count() {
        return new IntegerArgument();
    }
}
