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
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@CommandClass(name = "playlist", description = "Shows current playlist", translationKey = "music.command.playlist")
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

        info.reply("music.command.playlist.success").withPlaceholders(placeholders).queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
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

        String playlistStr = createTracksString(player.getPlaylist(), true, 100);

        info.logger().info("Playlist ({} tracks):\n{}", player.getPlaylistSize(), playlistStr);
    }

    @ArgumentMethod(name = "channel", description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
