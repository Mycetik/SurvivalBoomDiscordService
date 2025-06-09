package net.survivalboom.sbds.modules.music.commands.console;

import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.commands.AbstractPlayerCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Command(name = "playlist", description = "Print current playlist in the specified channel")
public class ConsolePlaylistCommand extends AbstractPlayerCommand implements ConsoleCommand {

    public ConsolePlaylistCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
        if (channel == null) {
            info.logger().error("Missing or invalid 'channel' argument.");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().error("No music player found for the specified channel.");
            return;
        }

        if (player.getPlaylistSize() == 0) {
            info.logger().info("The playlist is currently empty.");
            return;
        }

        String playlistStr = createTracksString(player.getPlaylist(), true, 100);

        info.logger().info("Playlist (" + player.getPlaylistSize() + " tracks):\n" + playlistStr);
    }

    @CommandArgument(name = "channel", description = "Voice channel where the bot is playing")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }
}
