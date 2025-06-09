package net.survivalboom.sbds.modules.music.bots;

import dev.arbjerg.lavalink.client.NodeOptions;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import net.survivalboom.sbds.api.utils.*;
import net.survivalboom.sbds.modules.music.MusicModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class BotManager extends Manager {


    private final MusicModule module;

    private final Logger logger;

    private final File botsFolder;


    private final IGuildRepositoryHandler guildRepository;

    private final NamespacedKey key;


    private final Set<MusicBot> musicBots = new HashSet<>();

    private final Set<NodeOptions> nodeInfos = new HashSet<>();


    public BotManager(@NotNull MusicModule module) {

        this.module = module;
        this.logger = module.getModule().getLogger();
        this.botsFolder = new File(module.getModule().getDataFolder(), "bots");

        this.guildRepository = module.getSbds().getDatabase().getRepositoryHandler("sbds:guilds", IGuildRepositoryHandler.class);
        this.key = NamespacedKey.fromModule(module, "music_module");

    }

    @Override
    protected void init0() {

        logger.info("Loading bot manager...");

        loadNodes();
        loadBots();

    }

    private void loadNodes() {
        List<TypeMap> nodes = CommonUtils.typeMap(module.getConfig().getMapList("nodes"));
        for (TypeMap map : nodes) {

            String name = map.getCastOrNull("name", String.class);
            String url = map.getCastOrNull("url", String.class);
            String password = map.getCastOrNull("password", String.class);

            if (name == null || url == null || password == null) continue;

            NodeOptions.Builder builder = new NodeOptions.Builder();
            builder.setName(name);
            builder.setServerUri(url);
            builder.setPassword(password);

            nodeInfos.add(builder.build());

        }
    }

    private void loadBots() {

        //noinspection ResultOfMethodCallIgnored
        botsFolder.mkdirs();

        for (File file : Objects.requireNonNull(botsFolder.listFiles())) {

            String fileName = file.getName();
            if (!fileName.endsWith(".token")) continue;

            String name = fileName.replace(".token", "");

            MusicBot bot;
            try {

                String token = loadToken(file);
                bot = new MusicBot(this, nodeInfos, name, token);

                bot.start();

            }

            catch (Throwable t) {
                logger.error("Failed to load music bot `{}`.", name, t);
                continue;
            }

            musicBots.add(bot);

        }

        logger.info("Loaded {} music bots.", musicBots.size());

    }

    private String loadToken(File file) throws IOException {

        byte[] bytes;
        try (FileInputStream stream = new FileInputStream(file)) {
            bytes = stream.readAllBytes();
        }

        return new String(bytes);

    }

    @Override
    protected void shutdown0() {

        nodeInfos.clear();

        for (MusicBot bot : getMusicBots()) {

            try {

                bot.shutdown();

            }

            catch (Throwable t) {
                logger.error("Failed to shutdown music bot `{}` properly. This may cause memory leaks!", bot.getName(), t);
                continue;
            }

            musicBots.remove(bot);

        }

    }


    public @NotNull MusicModule getModule() {
        return module;
    }


    public @NotNull List<MusicBot> getMusicBots() {
        return new ArrayList<>(musicBots);
    }

    public @NotNull List<MusicBot> findFreeBots(@NotNull AudioChannelUnion channel) {

        Objects.requireNonNull(channel, "channel == null");

        List<MusicBot> botsInGuild = findBotsInGuild(channel.getGuild());
        if (botsInGuild.isEmpty()) return botsInGuild;

        Guild guild = channel.getGuild();

        return botsInGuild.stream().filter(bot -> {
            GuildPlayer player = bot.getPlayer(guild);
            return player == null || !player.isActive();
        }).toList();

    }

    public @Nullable GuildPlayer findCurrentPlayer(@NotNull AudioChannelUnion channel) {
        Objects.requireNonNull(channel, "channel == null");
        Guild guild = channel.getGuild();
        return musicBots
                .stream()
                .map(bot -> bot.getPlayer(guild))
                .filter(Objects::nonNull)
                .filter(GuildPlayer::isActive)
                .filter(p -> channel.getIdLong() == Objects.requireNonNull(p.getChannel()).getIdLong())
                .findFirst()
                .orElse(null);
    }

    public @NotNull List<MusicBot> findBotsInGuild(@NotNull Guild guild) {

        List<MusicBot> bots = new ArrayList<>(musicBots.stream().filter(bot -> bot.getBot().getGuilds().contains(guild)).toList());
        Collections.shuffle(bots);

        return bots;

    }


    public boolean isMusicBanned(@NotNull Guild guild, @NotNull User user) {
        IGuildData guildData = guildRepository.createGuildData(guild);
        return Objects.requireNonNullElse((Boolean) guildData.container().getOrCreate(key).get(user.getId()), false);
    }

    public void setMusicBanned(@NotNull Guild guild, @NotNull User user, boolean state) {

        IGuildData guildData = guildRepository.createGuildData(guild);
        TypeMap map = guildData.container().getOrCreate(key);

        if (state) map.put(user.getId(), true);
        else map.remove(user.getId());

        guildData.save();

    }

}
