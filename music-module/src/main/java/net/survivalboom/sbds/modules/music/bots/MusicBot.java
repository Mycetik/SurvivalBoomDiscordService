package net.survivalboom.sbds.modules.music.bots;

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
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MusicBot {

    private static final Logger log = LoggerFactory.getLogger(MusicBot.class);

    private final JDABuilder jdaBuilder;

    private final BotManager manager;

    private final String name;

    private final LavalinkClient lavalink;

    private final Map<Guild, GuildPlayer> players = new HashMap<>();

    private JDA bot;

    public MusicBot(@NotNull BotManager manager, @NotNull Set<NodeOptions> nodeOptions, @NotNull String name, @NotNull String token) {

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

    public void start() {
        if (bot != null) throw new IllegalStateException("Already started");
        bot = jdaBuilder.build();
    }

    public void shutdown() {
        if (bot == null) throw new IllegalStateException("Not started");
        players.values().forEach(GuildPlayer::stopIfRunning);
        players.clear();
        lavalink.close();
        bot.shutdown();
        bot = null;
    }

    private void onTrackEnd(@NotNull TrackEndEvent event) {

        GuildPlayer player = players.values().stream().filter(p -> p.getGuild().getIdLong() == event.getGuildId()).findAny().orElse(null);
        if (player == null) {
            log.warn("[{}] Received TrackEndEvent but no GuildPlayer found that must receive that event!", event.getGuildId());
            return;
        }

        if (!player.isActive()) return;

        player.onTrackEnd(event);

    }


    public @NotNull GuildPlayer createPlayer(@NotNull Guild guild) {

        if (players.containsKey(guild)) return players.get(guild);

        Link link = lavalink.getOrCreateLink(guild.getIdLong());
        LavalinkPlayer lavalinkPlayer = link.createOrUpdatePlayer().block();
        Objects.requireNonNull(lavalinkPlayer, "lavalinkPlayer == null");

        GuildPlayer player = new GuildPlayer(this, guild, link, lavalinkPlayer);

        players.put(guild, player);

        return player;

    }


    public @Nullable GuildPlayer getPlayer(@NotNull Guild guild) {
        return players.get(guild);
    }


    public @NotNull JDA getBot() {
        return bot;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull BotManager getManager() {
        return manager;
    }


    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        bot.getPresence().setStatus(OnlineStatus.INVISIBLE);
    }


}
