package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.GreedyStringArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.MusicTrack;
import net.survivalboom.sbds.modules.music.music.TrackLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@CommandClass(name = "play", description = "Finds a track from your query and connects a new bot to your channel", translationKey = "music.command.play")
public class PlayCommand extends AbstractPlayerCommand implements SlashCommandExecutor {

    public PlayCommand(@NotNull MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        Member member = info.member();
        Objects.requireNonNull(member);

        // Знаходимо канал в якому сидить користувач //

        String query = info.arguments().getCast("query", String.class).orElseThrow();
        Objects.requireNonNull(query);

        // Шукаємо плеєр, який відповідає за цей сервер. Якщо немає, створюємо новий //

        GuildPlayer player = getPlayer(info, true, false);
        if (player == null) {
            return;
        }

        if (checkBannedOrLocked(info, player, false)) return;

        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannelUnion channel = Objects.requireNonNull(voiceState).getChannel();
        Objects.requireNonNull(channel);

        // Шукаємо треки за запитом й завантажуємо у плеєр //

        List<MusicTrack> tracks = searchTracks(info, query, player);
        if (tracks == null) {
            return;
        }

        // Під'єднуємо бота, який відповідає на плеєр й підключаємо його до голосового каналу.
        boolean newBot = player.isFresh();
        player.addTracks(tracks);

        // Готуємо плейсхолдери та відправляємо повідомлення, відповідно до того що ми зробили //

        User botUser = player.getBot().getBot().getSelfUser();
        MusicTrack addedTrack = tracks.getFirst();
        MusicTrack playingTrack = player.getCurrentPlaying();

        Placeholders placeholders = Placeholders.of(
                "bot", botUser,
                "playlist", createTracksString(player.getPlaylist(), 10),
                "playlist.size", player.getPlaylistSize(),
                "added", addedTrack,
                "playing", playingTrack
        );

        if (newBot) {
            info.reply("music.command.play.connected").withPlaceholders(placeholders).queue();
        }

        else {

            if (tracks.size() > 1) {
                placeholders.add("count", tracks.size());
                info.reply("music.command.play.playlist-added").withPlaceholders(placeholders).queue();
            }

            else {
                info.reply("music.command.play.playlist-added-single").withPlaceholders(placeholders).queue();
            }

        }


    }

    private boolean isUrl(@NotNull String string) {

        if (string.startsWith("https://") || string.startsWith("http://")) {

            try {
                new URI(string);
                return true;
            }

            catch (URISyntaxException e) {
                return false;
            }

        }

        return false;

    }

    private @Nullable List<MusicTrack> searchTracks(@NotNull SlashExecutionInfo info, @NotNull String query, @NotNull GuildPlayer player) {

        boolean isUrl = isUrl(query);

        if (isUrl) {
            info.reply("music.command.play.loading-tracks").withPlaceholders("url", query).queue();
        }
        else {
            info.reply("music.command.play.searching-tracks").withPlaceholders("query", query).queue();
        }

        List<MusicTrack> tracks;
        try {
            tracks = player.searchTracks(isUrl ? query : "ytsearch:" + query);
        }

        catch (TrackLoadException e) {
            info.reply("music.command.play.load-failed").withPlaceholders("error", e.toString()).queue();
            return null;
        }

        if (tracks.isEmpty()) {
            info.reply("music.command.play.no-tracks-found").withPlaceholders("query", query).queue();
            return null;
        }

        if (!isUrl) {
            return List.of(tracks.getFirst());
        }

        return tracks;

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        String query = info.arguments().getCast("query", String.class).orElseThrow();
        Objects.requireNonNull(query);

        // Шукаємо плеєр, який відповідає за цей сервер. Якщо немає, створюємо новий //
        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();
        Objects.requireNonNull(channel);

        GuildPlayer player = getPlayer(info, channel, true);
        if (player == null) {
            return;
        }

        // Шукаємо треки за запитом й завантажуємо у плеєр //

        List<MusicTrack> tracks = searchTracks(info, query, player);
        if (tracks == null) {
            return;
        }

        // Під'єднуємо бота, який відповідає на плеєр й підключаємо його до голосового каналу.
        boolean newBot = player.isFresh();
        player.addTracks(tracks);

        if (newBot) {
            player.idleDisconnect(false);
        }

        // Готуємо плейсхолдери та відправляємо повідомлення, відповідно до того що ми зробили //

        User botUser = player.getBot().getBot().getSelfUser();
        MusicTrack addedTrack = tracks.getFirst();

        if (newBot) {
            info.logger().info("Connected `{}` to `#{}`.", botUser.getEffectiveName(), channel.getName());
        }

        else {

            if (tracks.size() > 1) {
                info.logger().info("Added `{}` tracks to playlist!", tracks.size());
            }

            else {
                info.logger().info("Added `{}` to playlist!", addedTrack.getTitle());
            }

        }


    }

    private @Nullable List<MusicTrack> searchTracks(@NotNull ConsoleExecutionInfo info, @NotNull String query, @NotNull GuildPlayer player) {

        boolean isUrl = isUrl(query);

        if (isUrl) info.logger().info("Loading tracks from link `{}`...", query);
        else info.logger().info("Searching tracks for query `{}`...", query);

        List<MusicTrack> tracks;
        try {
            tracks = player.searchTracks(isUrl ? query : "ytsearch:" + query);
        }

        catch (TrackLoadException e) {
            return null;
        }

        if (tracks.isEmpty()) {
            info.logger().error("No results found for `{}`.", query);
            return null;
        }

        if (!isUrl) {
            return List.of(tracks.getFirst());
        }

        return tracks;

    }

    @ArgumentMethod(description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

    @ArgumentMethod(description = "URL or search query")
    public Argument<?> query() {
        return new GreedyStringArgument();
    }

}
