package net.survivalboom.sbds.modules.music.music;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.modules.music.MusicModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GuildPlayer extends Manager {

    private static final Logger log = LoggerFactory.getLogger(GuildPlayer.class);

    private final MusicModule musicModule;

    private final MusicBot bot;

    private final LavalinkPlayer lavalinkPlayer;

    private final Link lavalink;


    private final Guild guild;

    private final Member botGuildMember;

    private AudioChannelUnion channel;


    private final List<MusicTrack> playlist = new ArrayList<>();

    private int playingIndex = 0;

    private boolean playing = false;

    private boolean paused = false;


    private LoopMode loop = LoopMode.DISABLED;

    private boolean idleDisconnect = true;

    private boolean adminLock = false;


    private ISchedulerTask task;

    private boolean waitingForTracks = true;


    public GuildPlayer(
            @NotNull MusicBot bot,
            @NotNull Guild guild,
            @NotNull Link lavalink,
            @NotNull LavalinkPlayer lavalinkPlayer
    ) {

        this.guild = guild;
        this.bot = bot;

        this.botGuildMember = Objects.requireNonNull(bot.getBot().getGuildById(guild.getId())).getSelfMember();

        this.lavalink = lavalink;
        this.lavalinkPlayer = lavalinkPlayer;

        this.musicModule = bot.getManager().getModule();

    }

    //
    // MANAGER
    //

    /**
     * Запускає дискорд бота. Запускає усі необхідні tasks та підключає до голосового каналу.
     * Після виконання цього методу, бот може зупинитись якщо плейліст буде пустим.
     */
    @Override
    protected void init0() {

        // bot.getBot().getDirectAudioController().connect(channel);
        botGuildMember.getGuild().getAudioManager().openAudioConnection(channel);
        channel = null;

        this.playingIndex = -1;

        // Чекаємо поки Discord підключить нас, перед тим як прововжувати ініціалізацію.
        CommonUtils.waitUntil(() -> {
            updateCurrentChannel();
            return this.channel != null;
        }, 5000);

        this.playing = false;
        this.waitingForTracks = true;
        updateTrack();

        task = musicModule.getSbds().getScheduler().schedule(musicModule, bot.getName() + "-" + guild.getId() + "-MusicPlayer", this::task, 1000, 1000);

    }

    /**
     * Повністю зупинити музичного бота й відключити його від каналу.
     */
    @Override
    protected void shutdown0() {

        if (task != null) {
            task.tryCancel();
            task = null;
        }

//        bot.getBot().getDirectAudioController().disconnect(guild);
        botGuildMember.getGuild().getAudioManager().closeAudioConnection();

        this.channel = null;

        paused(false);
        this.loop = LoopMode.DISABLED;
        this.paused = false;
        this.idleDisconnect = true;

        this.playingIndex = 0;
        this.playlist.clear();

    }

    public void setChannel(@NotNull AudioChannelUnion channel) {

        if (isValid()) {
            throw new IllegalStateException("Could not set channel to active player");
        }

        this.channel = channel;

    }


    private void task() {

        updateCurrentChannel();

        // Не дає цьому плеєру від'єднатись, поки ми не завантажимо у нього треки.
        if (waitingForTracks) {
            return;
        }

        if (channel == null) {
            musicModule.getSbds().getScheduler().schedule(musicModule, this::shutdown, 0, 0); // Зупиняємо player через scheduler, щоб оминути нескінченне блокування
            return;
        }

        List<Member> members = getMembers();
        if ((members.size() == 1 || members.stream().allMatch(m -> m.getUser().isBot())) && idleDisconnect) {
            musicModule.getSbds().getScheduler().schedule(musicModule, this::shutdown, 0, 0);
            return;
        }

        if (!isPlaying()) {

            if (isLastTrack()) {

                if (loop == LoopMode.PLAYLIST || !idleDisconnect) {
                    this.playingIndex = -1;
                }

                else if (loop == LoopMode.DISABLED) {
                    musicModule.getSbds().getScheduler().schedule(musicModule, this::shutdown, 0, 0);
                    return;
                }

            }

            if (loop != LoopMode.TRACK) {
                playingIndex++;
            }

            updateTrack();
            this.playing = true;

        }

    }

    private List<Member> getMembers() {
        return Objects.requireNonNull(getBot().getManager().getModule().getSbds().getBot().getChannelById(AudioChannel.class, channel.getId())).getMembers();
    }

    public void onTrackEnd(@NotNull TrackEndEvent event) {

        if (!event.getEndReason().getMayStartNext()) {
            return;
        }

        playing = false;

    }

    private void updateCurrentChannel() {

        var state = botGuildMember.getVoiceState();
        if (state == null) {
            this.channel = null;
            return;
        }

        this.channel = botGuildMember.getVoiceState().getChannel();

    }

    private void updateTrack() {

        MusicTrack musicTrack = getCurrentPlaying();
        Track track = musicTrack != null ? musicTrack.getTrack() : null;

        lavalinkPlayer.setTrack(track).block(Duration.ofSeconds(5000));

    }

    //
    // TRACKS
    //

    public @NotNull List<MusicTrack> searchTracks(@NotNull String query) throws TrackLoadException {

        Objects.requireNonNull(query, "query == null");
        checkValid();

        LavalinkLoadResult result = lavalink.loadItem(query).block();
        Objects.requireNonNull(result);

        List<Track> tracks = switch (result) {

            // Трек завантажено з прямого посилання //
            case TrackLoaded trackLoaded -> List.of(trackLoaded.getTrack());

            // Декілька треків знайдено за цим запитом //
            case PlaylistLoaded playlistLoaded -> playlistLoaded.getTracks();

            // Нічого не знайдено //
            case NoMatches ignored -> List.of();

            // Сталась помилка при спробі завантажити треки //
            case LoadFailed loadFailed -> throw new TrackLoadException(loadFailed.getException().toString());

            // Знайдено треки на якійсь платформі //
            case SearchResult searchResult -> searchResult.getTracks();

            default -> throw new IllegalStateException("Unknown LavalinkLoadResult `" + result + "`");

        };

        return tracks.stream()
                .map(MusicTrack::new)
                .collect(Collectors.toList());

    }

    public void addTracks(@NotNull List<MusicTrack> tracks) {

        Objects.requireNonNull(tracks, "tracks == null");
        checkValid();

        this.playlist.addAll(tracks);
        this.waitingForTracks = false;

    }

    public void changePlayingIndex(int steps) {

        checkValid();

        int nextPlayingIndex = this.playingIndex + steps;
        if (nextPlayingIndex >= playlist.size() || nextPlayingIndex < 0) {
            throw new IllegalArgumentException("Invalid playing index `" + nextPlayingIndex + "`");
        }

        this.playingIndex += steps;
        updateTrack();

    }

    public void setPlayingIndex(int index) {

        checkValid();

        if (index >= playlist.size()) {
            throw new IllegalArgumentException("index >= playlist.size()");
        }

        if (index < 0) {
            throw new IllegalArgumentException("index is negative");
        }

        this.playingIndex = index;
        updateTrack();

    }


    public @Nullable MusicTrack getCurrentPlaying() {

        if (playlist.isEmpty() || playingIndex >= playlist.size()) {
            return null;
        }

        return playlist.get(playingIndex);

    }

    public int getPlayingIndex() {
        return playingIndex;
    }

    public @NotNull List<MusicTrack> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    public int getPlaylistSize() {
        return playlist.size();
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isLastTrack() {
        return playingIndex + 1 >= playlist.size();
    }

    //
    // STATE
    //

    // pause //

    public void paused(boolean v) {
        checkValid();
        lavalinkPlayer.setPaused(v).subscribe();
        paused = v;
    }

    public boolean paused() {
        return paused;
    }

    // loop //

    public void loop(@NotNull LoopMode loop) {
        checkValid();
        this.loop = loop;
    }

    public @NotNull LoopMode loop() {
        return loop;
    }

    // idle disconnect //

    public void idleDisconnect(boolean v) {
        checkValid();
        this.idleDisconnect = v;
    }

    public boolean idleDisconnect() {
        return idleDisconnect;
    }

    // admin lock //

    public void adminLock(boolean v) {
        checkValid();
        this.adminLock = v;
    }

    public boolean adminLock() {
        return adminLock;
    }

    //
    // GETTERS
    //

    public boolean isFresh() {
        return waitingForTracks;
    }


    public @NotNull MusicBot getBot() {
        return bot;
    }

    public @NotNull Guild getGuild() {
        return guild;
    }

    public @Nullable AudioChannelUnion getChannel() {
        return channel;
    }

}
