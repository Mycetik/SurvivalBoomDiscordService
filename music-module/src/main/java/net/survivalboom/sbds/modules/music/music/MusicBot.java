package net.survivalboom.sbds.modules.music.music;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.survivalboom.sbds.api.utils.valid.Manager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

public class MusicBot extends Manager {

    private static final Logger log = LoggerFactory.getLogger(MusicBot.class);

    private final JDABuilder jdaBuilder;

    private final MusicManager manager;

    private final String name;

    private final LavalinkClient lavalink;

    private final Map<Guild, GuildPlayer> players = new WeakHashMap<>();

    private JDA bot;

    public MusicBot(@NotNull MusicManager manager, @NotNull Set<NodeOptions> nodeOptions, @NotNull String name, @NotNull String token) {

        this.name = name;
        this.manager = manager;

        this.lavalink = new LavalinkClient(Helpers.getUserIdFromToken(token));
        nodeOptions.forEach(lavalink::addNode);

        lavalink.on(TrackEndEvent.class).subscribe(this::onTrackEnd);

        jdaBuilder = JDABuilder
                .createLight(token)
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalink))
                .setEventManager(new AnnotatedEventManager())
                .addEventListeners(this);

    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {
        bot = jdaBuilder.build();
    }

    @Override
    protected void shutdown0() {

        players.values().forEach(GuildPlayer::shutdownIfNeeded);
        players.clear();

        lavalink.close();

        bot.shutdown();
        bot = null;

    }

    private void onTrackEnd(@NotNull TrackEndEvent event) {

        GuildPlayer player = players.values().stream()
                .filter(p -> p.getGuild().getIdLong() == event.getGuildId())
                .findAny()
                .orElse(null);

        if (player == null) {
            log.warn("[{}] Received TrackEndEvent but no GuildPlayer found that must receive that event!", event.getGuildId());
            return;
        }

        if (!player.isValid()) {
            return;
        }

        player.onTrackEnd(event);

    }

    //
    // PLAYER
    //

    public @NotNull GuildPlayer createPlayer(@NotNull AudioChannelUnion channel) {

        Objects.requireNonNull(channel, "channel == null");
        checkValid();

        Guild guild = channel.getGuild();

        GuildPlayer player = getPlayer(guild);
        if (player == null) {

            Link link = lavalink.getOrCreateLink(guild.getIdLong());
            LavalinkPlayer lavalinkPlayer = link.createOrUpdatePlayer().block(Duration.ofSeconds(5000));
            Objects.requireNonNull(lavalinkPlayer, "lavalinkPlayer == null; did something just break?");

            player = new GuildPlayer(this, guild, link, lavalinkPlayer);

            players.put(guild, player);

        }

        if (!player.isValid()) {
            player.setChannel(channel);
            player.init();
        }

        return player;

    }

    public @Nullable GuildPlayer getPlayer(@NotNull Guild guild) {
        return players.get(guild);
    }

    public @NotNull List<GuildPlayer> getPlayers() {
        return new ArrayList<>(players.values());
    }

    //
    // GETTERS
    //

    public @NotNull JDA getBot() {
        return bot;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull MusicManager getManager() {
        return manager;
    }


    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        bot.getPresence().setStatus(OnlineStatus.INVISIBLE);
    }


}
