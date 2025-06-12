package net.survivalboom.sbds.modules.music.commands.console;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.bots.TrackLoadException;
import net.survivalboom.sbds.modules.music.commands.AbstractPlayerCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@Command(name = "play", description = "Finds music and adds it to the playlist, or connects a new music bot")
public class ConsolePlayCommand extends AbstractPlayerCommand implements ConsoleCommand {

    public ConsolePlayCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        // Знаходимо канал в якому сидить користувач //

        String query = info.arguments().get("query", String.class);
        Objects.requireNonNull(query);

        // Шукаємо плеєр, який відповідає за цей сервер. Якщо немає, створюємо новий //
        AudioChannelUnion channel = null;
        if (info.arguments().get("channel", AudioChannelUnion.class) != null) {
            channel = info.arguments().get("channel", AudioChannelUnion.class);
        }
        else {
            info.logger().info("No channel specified");
        }
        assert channel != null;

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
        TrackInfo playingTrack = Objects.requireNonNull(player.getCurrentPlaying(), "track == null, track is not playing? what?").getInfo();

        Placeholders placeholders = new Placeholders()
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl())

                .add("{ADDED-NAME}", addedTrack.getTitle())
                .add("{ADDED-DURATION}", "6:66:66")
                .add("{ADDED-SOURCE}", addedTrack.getSourceName())
                .add("{ADDED-LINK}", addedTrack.getUri())

                .add("{TRACKS-COUNT}", tracks.size())
                .add("{TRACKS}", createTracksString(tracks, false, 10))

                .add("{PLAYING-NAME}", playingTrack.getTitle())
                .add("{PLAYING-SOURCE}", playingTrack.getSourceName())
                .add("{PLAYING-LINK}", playingTrack.getUri())

                .add("{PLAYLIST-SIZE}", player.getPlaylist().size())
                .add("{PLAYLIST}", createTracksString(player.getPlaylist(), true, 10));

        if (newBot) info.logger().info("Bot has connected");

        else {

            if (tracks.size() > 1) {
                placeholders.add("{TRACKS-SIZE}", tracks.size());
                info.logger().info("Додано `{}` треків у плейліст!", placeholders.get("TRACKS-COUNT"));
            }

            else {
                info.logger().info("Додано у плейліст!");
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

    private @Nullable List<Track> searchTracks(@NotNull ConsoleExecutionInfo info, @NotNull String query, @NotNull GuildPlayer player) {

        boolean isUrl = isUrl(query);

        if (isUrl) info.logger().info("I'm downloading the tracks from the link {}", query);
        else info.logger().info("Looking for tracks on request {}", query);

        List<Track> tracks;
        try {
            tracks = player.searchTracks(isUrl ? query : "ytsearch:" + query);
        }

        catch (TrackLoadException e) {
            return null;
        }

        if (tracks.isEmpty()) {
            info.logger().info("No results found for {}!", query);
            return null;
        }

        if (!isUrl) {
            return List.of(tracks.getFirst());
        }

        return tracks;

    }

    @CommandArgument(name = "query", description = "URL or search query")
    public Argument<?> song() {
        return new StringArgument();
    }

    @CommandArgument(name = "channel", description = "123")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }
}
