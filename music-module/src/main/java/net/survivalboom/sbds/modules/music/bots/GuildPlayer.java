package net.survivalboom.sbds.modules.music.bots;

import dev.arbjerg.lavalink.client.Link;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuildPlayer {

    private static final Logger log = LoggerFactory.getLogger(GuildPlayer.class);
    private final Guild guild;

    private final LavalinkPlayer lavalinkPlayer;

    private final Link link;

    private final MusicBot bot;

    private final MusicModule module;


    private final List<Track> playlist = new ArrayList<>();

    private int playingIndex = 0;


    private Loop loop = Loop.NO;

    private boolean idleDisconnect = true;


    private ISchedulerTask task;

    private AudioChannelUnion channel = null;


    public GuildPlayer(@NotNull MusicBot bot, @NotNull Guild guild, @NotNull Link link, @NotNull LavalinkPlayer lavalinkPlayer) {

        Objects.requireNonNull(guild);
        Objects.requireNonNull(link);
        Objects.requireNonNull(lavalinkPlayer);
        Objects.requireNonNull(bot);

        this.guild = guild;
        this.bot = bot;

        this.link = link;
        this.lavalinkPlayer = lavalinkPlayer;

        this.module = bot.getManager().getModule();

    }

    private void updateChannel() {
        this.channel = Objects.requireNonNull(Objects.requireNonNull(bot.getBot().getGuildById(guild.getId())).getSelfMember().getVoiceState()).getChannel();
    }

    private void launchTask() {
        task = module.getSbds().getScheduler().schedule(module, bot.getName() + guild.getId() + "-Player", this::task, 1000, 1000);
    }

    private void task() {

        updateChannel();

        if (channel == null) {
            stop();
            return;
        }

        if (getMembers().stream().allMatch(u -> u.getUser().isBot())) {
            stop();
            return;
        }

    }

    private List<Member> getMembers() {
        return Objects.requireNonNull(getBot().getManager().getModule().getSbds().getBot().getChannelById(AudioChannel.class, channel.getId())).getMembers();
    }

    //
    // PLAYER
    //

    // TRACKS //

    public @NotNull List<Track> searchTracks(@NotNull String query) throws TrackLoadException {

        LavalinkLoadResult result = link.loadItem(query).block();
        Objects.requireNonNull(result);

        return switch (result) {

            case TrackLoaded trackLoaded -> new ArrayList<>(List.of(trackLoaded.getTrack()));

            case PlaylistLoaded playlistLoaded -> playlistLoaded.getTracks();

            case NoMatches ignored -> new ArrayList<>();

            case LoadFailed loadFailed -> throw new TrackLoadException(loadFailed.getException().toString());

            default -> throw new IllegalStateException("Unknown LavalinkLoadResult `" + result + "`");

        };


    }


    public void addTracks(@NotNull List<Track> tracks) {
        if (channel == null) throw new IllegalStateException("not running");
        playlist.addAll(tracks);

        if (isActive()) return;

        launchTask();
        update();

    }

    public boolean skip(int steps) {
        if (task == null) throw new IllegalStateException("not running");

        if (loop == Loop.TRACK) loop = Loop.NO;

        if (playingIndex >= playlist.size()) {

            if (loop != Loop.PLAYLIST) {
                stop();
                return false;
            }

            else playingIndex = 0;

        }

        this.playingIndex += steps;

        update();

        return true;

    }

    public boolean back(int steps) {
        if (task == null) throw new IllegalStateException("not running");

        if (playingIndex < 1) {

            if (loop != Loop.PLAYLIST && !playlist.isEmpty()) playingIndex = playlist.size() - 1;
            else return false;

        }

        this.playingIndex -= steps;

        update();

        return true;

    }

    // OPTIONS //

    public void setPlayingIndex(int index) {
        if (task == null) throw new IllegalStateException("not running");
        if (index >= playlist.size()) throw new IllegalArgumentException("index >= playlist.size()");
        this.playingIndex = index;
        update();
    }

    public void setIdleDisconnect(boolean v) {
        if (task == null) throw new IllegalStateException("not running");
        this.idleDisconnect = v;
    }

    public void loop(@NotNull Loop loop) {
        if (task == null) throw new IllegalStateException("not running");
        Objects.requireNonNull(loop, "loop == null");
        this.loop = loop;
    }

    public void pause(boolean v) {
        if (task == null) throw new IllegalStateException("not running");
        lavalinkPlayer.setPaused(v).subscribe();
    }

    // STATES //

    public void update() {
        lavalinkPlayer.setTrack(getCurrentPlaying()).subscribe();
    }

    public void stopIfRunning() {
        if (!isActive()) return;
        stop();
    }

    public void stop() {

        if (task == null) throw new IllegalStateException("not running");

        task.cancel();

        bot.getBot().getDirectAudioController().disconnect(guild);

        pause(false);

        this.channel = null;
        this.loop = Loop.NO;
        this.idleDisconnect = true;
        this.playingIndex = 0;
        this.playlist.clear();

        task = null;

    }

    public void connect(@NotNull AudioChannelUnion channel) {

        if (this.channel != null) throw new IllegalStateException("Already connected");

        if (!channel.getGuild().equals(guild)) throw new IllegalArgumentException("You tried to connect to a channel that is not from the guild used by this player");
        bot.getBot().getDirectAudioController().connect(channel);

        CommonUtils.waitUntil(() -> {
            updateChannel();
            return this.channel != null;
        }, 1000);

    }

    // GETTERS //

    public int getPlayingIndex() {
        return playingIndex;
    }

    public @Nullable Track getCurrentPlaying() {
        if (playingIndex >= playlist.size()) return null;
        return playlist.get(playingIndex);
    }

    public boolean isPaused() {
        return lavalinkPlayer.getPaused();
    }

    public boolean isActive() {
        return task != null;
    }


    //
    // INFO GETTERS
    //

    public @Nullable AudioChannelUnion getChannel() {
        return channel;
    }

    public @NotNull Guild getGuild() {
        return guild;
    }

    public @NotNull MusicBot getBot() {
        return bot;
    }

    public enum Loop {
        NO,
        TRACK,
        PLAYLIST,
    }

}
