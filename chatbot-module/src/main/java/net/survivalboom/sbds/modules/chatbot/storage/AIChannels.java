package net.survivalboom.sbds.modules.chatbot.storage;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.chatbot.ChatBotModule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

public class AIChannels {

    private static final String AI_CHANNELS_KEY = "ai-channels";

    private final IGuildRepositoryHandler repository;

    private final Map<Long, Boolean> cache = new WeakHashMap<>();


    public AIChannels(@NotNull ModuleMain module) {
        this.repository = module.getDatabase().getRepositoryHandler("sbds:guilds", IGuildRepositoryHandler.class);
    }


    public @NotNull CompletableFuture<Boolean> isAiChannel(@NotNull MessageChannel channel) {

        Guild guild = ((GuildChannel) channel).getGuild();
        long id = channel.getIdLong();

        return repository.getGuildData(guild).thenApply(gd -> cache.computeIfAbsent(id, k -> {

            if (gd == null) {
                return null;
            }

            var container = gd.container().get(ChatBotModule.KEY);
            if (container == null) {
                return null;
            }

            List<?> channels = container.getCastOrNull(AI_CHANNELS_KEY, List.class);

            return channels != null && channels.contains(id);

        }));

    }

    public @NotNull CompletableFuture<Void> setAiChannel(@NotNull MessageChannel channel, boolean value) {

        Guild guild = ((GuildChannel) channel).getGuild();

        return repository.createGuildData(guild).thenAccept(gd -> {

            var container = gd.container().getOrCreate(ChatBotModule.KEY);

            @SuppressWarnings("unchecked")
            List<Long> channels = container.getCastOrNull(AI_CHANNELS_KEY, List.class);
            if (channels == null) {
                channels = new ArrayList<>();
                container.put(AI_CHANNELS_KEY, channels);
            }

            long id = channel.getIdLong();
            if (value) channels.add(id);
            else channels.remove(id);

            cache.put(id, value);

            gd.save();

        });

    }

}
