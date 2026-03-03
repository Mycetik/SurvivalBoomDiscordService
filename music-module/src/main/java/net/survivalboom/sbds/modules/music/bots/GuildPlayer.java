package net.survivalboom.sbds.modules.music.bots;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.modules.music.MusicModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuildPlayer {

    private static final Logger log = LoggerFactory.getLogger(GuildPlayer.class);

    private final MusicModule musicModule;

    private final MusicBot bot;

    private final LavalinkPlayer lavalinkPlayer;

    private final Link lavalink;


    private final Guild guild;

    private final Member botGuildMember;

    private AudioChannelUnion channel = null;


    private final List<Track> playlist = new ArrayList<>();

    private int playingIndex = 0;

    private boolean playing = false;

    private boolean paused = false;


    private LoopMode loop = LoopMode.DISABLED;

    private boolean idleDisconnect = true;

    private boolean adminLock = false;


    private ISchedulerTask task;


    public GuildPlayer(@NotNull MusicBot bot, @NotNull Guild guild, @NotNull Link lavalink, @NotNull LavalinkPlayer lavalinkPlayer) {

        this.guild = guild;
        this.bot = bot;

        this.botGuildMember = Objects.requireNonNull(bot.getBot().getGuildById(guild.getId())).getSelfMember();

        this.lavalink = lavalink;
        this.lavalinkPlayer = lavalinkPlayer;

        this.musicModule = bot.getManager().getModule();

    }


    public @NotNull List<Track> searchTracks(@NotNull String query) throws TrackLoadException {

        LavalinkLoadResult result = lavalink.loadItem(query).block();
        Objects.requireNonNull(result);

        return switch (result) {

            // Трек завантажено з прямого посилання //
            case TrackLoaded trackLoaded -> new ArrayList<>(List.of(trackLoaded.getTrack()));

            // Декілька треків знайдено за цим запитом //
            case PlaylistLoaded playlistLoaded -> playlistLoaded.getTracks();

            // Нічого не знайдено //
            case NoMatches ignored -> new ArrayList<>();

            // Сталась помилка при спробі завантажити треки //
            case LoadFailed loadFailed -> throw new TrackLoadException(loadFailed.getException().toString());

            // Знайдено треки на якійсь платформі //
            case SearchResult searchResult -> searchResult.getTracks();

            default -> throw new IllegalStateException("Unknown LavalinkLoadResult `" + result + "`");

        };


    }

    //
    // PLAYER
    //

    /**
     * Підключає бота у вказаний канал.
     * @param channel Канал куди потрібно підключити бота.
     */
    public void connect(@NotNull AudioChannelUnion channel) {

        Objects.requireNonNull(channel, "channel == null");

        if (!channel.getGuild().equals(guild)) {
            throw new IllegalArgumentException("Invalid guild for this player");
        }

        if (channel.equals(this.channel)) {
            throw new IllegalStateException("Already connected to this channel");
        }

//        bot.getBot().getDirectAudioController().connect(channel);
        botGuildMember.getGuild().getAudioManager().openAudioConnection(channel);

        CommonUtils.waitUntil(() -> {
            updateCurrentChannel();
            return this.channel != null;
        }, 5000);

    }

    /**
     * Запускає дискорд бота. Запускає усі необхідні tasks.
     * Після виконання цього методу, бот може зупинитись якщо плейліст буде пустим.
     * Тому потрібно <b>спочатку підключити бота й додати треки в плейліст</b>, а потім вже викликати цей метод.
     */
    public void launch() {

        this.playing = true;
        updateTrack();

        task = musicModule.getSbds().getScheduler().schedule(musicModule, bot.getName() + "-" + guild.getName() + "-MusicPlayer", this::task, 1000, 1000);

    }

    /**
     * Додає треки до плейлісту бота.
     * @param tracks Список з треків.
     */
    public void addTracks(@NotNull List<Track> tracks) {
        Objects.requireNonNull(tracks, "tracks == null");
        this.playlist.addAll(tracks);
    }

    /**
     * Повністю зупинити музчного бота й відключити його від каналу.
     */
    public void stop() {

        if (task != null) {
            task.cancelAndWait(1000, true);
            task = null;
        }

//        bot.getBot().getDirectAudioController().disconnect(guild);
        botGuildMember.getGuild().getAudioManager().closeAudioConnection();

        this.channel = null;

        setPaused(false);
        this.loop = LoopMode.DISABLED;
        this.paused = false;
        this.idleDisconnect = true;

        this.playingIndex = 0;
        this.playlist.clear();

    }

    /**
     * Змістити індекс поточної пісні у плейлісті.
     * @param steps Зміщення індексу. Має бути не більше і не менше за плейліст.
     */
    public void changePlayingIndex(int steps) {
        int nextPlayingIndex = this.playingIndex + steps;
        if (nextPlayingIndex >= playlist.size() || nextPlayingIndex < 0) throw new IllegalArgumentException("Invalid playing index `" + nextPlayingIndex + "`");
        this.playingIndex += steps;
        updateTrack();
    }

    /**
     * Встановити індекс поточної пісні у плейлісті.
     * @param index Новий індекс.
     */
    public void setPlayingIndex(int index) {
        if (index >= playlist.size()) throw new IllegalArgumentException("index >= playlist.size()");
        if (index < 0) throw new IllegalArgumentException("index is negative");
        this.playingIndex = index;
        updateTrack();
    }

    /**
     * Зупинити/продовжити поточний трек.
     * @param v true - зупинити, false - продовжити.
     */
    public void setPaused(boolean v) {
        lavalinkPlayer.setPaused(v).subscribe();
        paused = v;
    }


    //
    // STATE
    //

    /**
     * Перемкнути режим повторення треків.
     * @param loop Режим повторення.
     */
    public void loop(@NotNull LoopMode loop) {
        this.loop = loop;
    }

    /**
     * @return Поточний встановлений режим повторення.
     */
    public @NotNull LoopMode loop() {
        return loop;
    }

    /**
     * Перемкнути зупинення музичного бота, при закінченні плейліста.
     * @param v true - увімкнути, false - вимкнути.
     */
    public void idleDisconnect(boolean v) {
        this.idleDisconnect = v;
    }

    public boolean idleDisconnect() {
        return idleDisconnect;
    }

    /**
     * Перемкнути доступ звичайних користувачів до музичного бота.
     * @param v true - дозволити тільки адміністраторам, false - дозволити усім.
     */
    public void adminLock(boolean v) {
        this.adminLock = v;
    }

    public boolean adminLock() {
        return adminLock;
    }


    //
    // GETTERS
    //

    public @Nullable Track getCurrentPlaying() {
        if (playlist.isEmpty() || playingIndex >= playlist.size()) return null;
        return playlist.get(playingIndex);
    }

    public int getPlayingIndex() {
        return playingIndex;
    }

    public @NotNull List<Track> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    public int getPlaylistSize() {
        return playlist.size();
    }

    public boolean isActive() {
        return task != null;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isLastTrack() {
        return playingIndex + 1 >= playlist.size();
    }

    public boolean isPaused() {
        return paused;
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



    //
    // HANDLERS
    //

    public void onTrackEnd(@NotNull TrackEndEvent event) {
        if (!event.getEndReason().getMayStartNext()) return;
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
        lavalinkPlayer.setTrack(getCurrentPlaying()).block(Duration.ofSeconds(5000));
    }

    private void task() {

        updateCurrentChannel();

        if (channel == null) {
            stop();
            return;
        }

        List<Member> members = getMembers();
        if ((members.size() == 1 || members.stream().allMatch(m -> m.getUser().isBot())) && idleDisconnect) {
            stop();
            return;
        }

        if (!isPlaying()) {

            if (isLastTrack()) {

                if (loop == LoopMode.PLAYLIST || !idleDisconnect) {
                    this.playingIndex = -1;
                }

                else if (loop == LoopMode.DISABLED) {
                    stop();
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

}
