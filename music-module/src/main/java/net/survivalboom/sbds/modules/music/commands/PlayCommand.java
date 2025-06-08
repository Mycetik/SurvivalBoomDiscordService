package net.survivalboom.sbds.modules.music.commands;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.bots.MusicBot;
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

        Member member = info.guildMember();
        Objects.requireNonNull(member);

        // Знаходимо канал в якому сидить користувач //

        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannelUnion channel = Objects.requireNonNull(voiceState).getChannel();
        if (channel == null) {
            info.reply("music.command.not-in-voice").queue();
            return;
        }

        String query = info.arguments().get("query", String.class);
        Objects.requireNonNull(query);

        // Шукаємо плеєр, який відповідає за цей сервер. Якщо немає, створюємо новий //

        GuildPlayer player = getPlayer(info, true);
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
        if (newBot) player.connect(channel);
        player.addTracks(tracks);

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
                .add("{TRACKS}", createTracksString(tracks, false, 5))

                .add("{PLAYING-NAME}", playingTrack.getTitle())
                .add("{PLAYING-DURATION}", "6:66:66")
                .add("{PLAYING-SOURCE}", playingTrack.getSourceName())
                .add("{PLAYING-LINK}", playingTrack.getUri())

                .add("{PLAYLIST-SIZE}", player.getPlaylist().size())
                .add("{PLAYLIST}", createTracksString(tracks, true, 5));

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

    private @NotNull String createTracksString(@NotNull List<Track> tracks, boolean withIndex, int max) {

        StringBuilder builder = new StringBuilder();
        int i = 1;
        for (Track ignored : tracks) {

            if (i > tracks.size()) break;

            if (i >= max && i < tracks.size()) {
                builder.append("- `..").append(tracks.size() - max).append("..`\n");
                i = tracks.size();
                continue;
            }

            TrackInfo info = tracks.get(i - 1).getInfo();
            Placeholders placeholders = new Placeholders();
            placeholders
                    .add("{INDEX}", i)
                    .add("{NAME}", info.getTitle())
                    .add("{DURATION}", info.getLength())
                    .add("{SOURCE}", info.getSourceName())
                    .add("{LINK}", info.getUri());

            if (withIndex) builder.append(placeholders.parse("`{INDEX}.` **[{NAME}]({LINK})** `{DURATION}`\n"));
            else builder.append(placeholders.parse("- **[{NAME}]({LINK})** `{DURATION}`\n"));

            i++;

        }

        return builder.toString();

    }

    @CommandArgument(name = "query", description = "URL or search query")
    public Argument<?> song() {
        return new StringArgument();
    }

}
