package net.survivalboom.sbds.modules.music.commands;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.bots.TrackLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@Command(name = "play", description = "Finds music and adds it to the playlist, or connects a new music bot")
public class PlayCommand extends AbstractPlayerCommand implements SlashCommand {

    public PlayCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        Member member = info.member();
        Objects.requireNonNull(member);

        // Знаходимо канал в якому сидить користувач //

        String query = info.arguments().get("query", String.class);
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

        List<Track> tracks = searchTracks(info, query, player);
        if (tracks == null) {
            return;
        }

        // Під'єднуємо бота, який відповідає на плеєр й підключаємо його до голосового каналу.
        boolean newBot = !player.isActive();
        player.addTracks(tracks);

        if (newBot) {
            player.connect(channel);
            player.launch();
        }

        // Готуємо плейсхолдери та відправляємо повідомлення, відповідно до того що ми зробили //

        User botUser = player.getBot().getBot().getSelfUser();
        TrackInfo addedTrack = tracks.getFirst().getInfo();
        TrackInfo playingTrack = Objects.requireNonNull(player.getCurrentPlaying(), "track == null, track is not playing? what?").getInfo();

        Placeholders placeholders = new Placeholders()
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl())

                .add("{ADDED-NAME}", addedTrack.getTitle())
                .add("{ADDED-DURATION}", formatTime(addedTrack.getLength()))
                .add("{ADDED-SOURCE}", addedTrack.getSourceName())
                .add("{ADDED-LINK}", addedTrack.getUri())

                .add("{TRACKS-COUNT}", tracks.size())
                .add("{TRACKS}", createTracksString(tracks, false, 10))

                .add("{PLAYING-NAME}", playingTrack.getTitle())
                .add("{PLAYING-DURATION}", formatTime(playingTrack.getLength()))
                .add("{PLAYING-SOURCE}", playingTrack.getSourceName())
                .add("{PLAYING-LINK}", playingTrack.getUri())

                .add("{PLAYLIST-SIZE}", player.getPlaylist().size())
                .add("{PLAYLIST}", createTracksString(player.getPlaylist(), true, 10));

        if (newBot) info.editHook("music.command.play.connected").withPlaceholders(placeholders).queue();

        else {

            if (tracks.size() > 1) {
                placeholders.add("{TRACKS-SIZE}", tracks.size());
                info.editHook("music.command.play.playlist-added").withPlaceholders(placeholders).queue();
            }

            else {
                info.editHook("music.command.play.playlist-added-single").withPlaceholders(placeholders).queue();
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

    private @Nullable List<Track> searchTracks(@NotNull SlashExecutionInfo info, @NotNull String query, @NotNull GuildPlayer player) {

        boolean isUrl = isUrl(query);

        if (isUrl) info.reply("music.command.play.loading-tracks").withPlaceholders("{URL}", query).queue();
        else info.reply("music.command.play.searching-tracks").withPlaceholders("{QUERY}", query).queue();

        List<Track> tracks;
        try {
            tracks = player.searchTracks(isUrl ? query : "ytsearch:" + query);
        }

        catch (TrackLoadException e) {
            info.editHook("music.command.play.load-failed").withPlaceholders("{ERROR}", e.toString()).queue();
            return null;
        }

        if (tracks.isEmpty()) {
            info.editHook("music.command.play.no-tracks-found").withPlaceholders("{QUERY}", query).queue();
            return null;
        }

        if (!isUrl) {
            return List.of(tracks.getFirst());
        }

        return tracks;

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        String query = info.arguments().get("query", String.class);
        Objects.requireNonNull(query);

        // Шукаємо плеєр, який відповідає за цей сервер. Якщо немає, створюємо новий //
        AudioChannelUnion channel = info.arguments().getCastOrNull("channel", AudioChannelUnion.class);
        Objects.requireNonNull(channel);

        GuildPlayer player = getPlayer(info, channel, true);
        if (player == null) {
            return;
        }

        // Шукаємо треки за запитом й завантажуємо у плеєр //

        List<Track> tracks = searchTracks(info, query, player);
        if (tracks == null) {
            return;
        }

        // Під'єднуємо бота, який відповідає на плеєр й підключаємо його до голосового каналу.
        boolean newBot = !player.isActive();
        player.addTracks(tracks);

        if (newBot) {
            player.connect(channel);
            player.launch();
        }

        // Готуємо плейсхолдери та відправляємо повідомлення, відповідно до того що ми зробили //

        User botUser = player.getBot().getBot().getSelfUser();
        TrackInfo addedTrack = tracks.getFirst().getInfo();

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

    private @Nullable List<Track> searchTracks(@NotNull ConsoleExecutionInfo info, @NotNull String query, @NotNull GuildPlayer player) {

        boolean isUrl = isUrl(query);

        if (isUrl) info.logger().info("Loading tracks from link `{}`...", query);
        else info.logger().info("Searching tracks for query `{}`...", query);

        List<Track> tracks;
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

    @CommandArgument(name = "channel", description = "123", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

    @CommandArgument(name = "query", description = "URL or search query")
    public Argument<?> song() {
        return new StringArgument();
    }

}
