package net.survivalboom.sbds.modules.chatbot.storage;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllowedChannels extends Manager {

    private final IDatabase database;


    private final Map<TextChannel, Boolean> allowedChannels = new HashMap<>();

    private final NamespacedKey key;


    private IGuildRepositoryHandler repository;


    public AllowedChannels(@NotNull ModuleMain module) {
        this.database = module.getDatabase();
        this.key = NamespacedKey.fromModule(module, "allowed_channels");
    }


    @Override
    protected void init0() {
        repository = database.getRepositoryHandler("sbds:guilds", IGuildRepositoryHandler.class);
    }

    @Override
    protected void shutdown0() {
        repository = null;
    }


    @SuppressWarnings("unchecked")
    public boolean isAllowedChannel(@NotNull TextChannel channel) {

        checkValid();

        return allowedChannels.computeIfAbsent(channel, key -> {

            IGuildData guildData = repository.createGuildData(key.getGuild());
            TypeMap map = guildData.container().getOrCreate(this.key);
            List<String> channels = (List<String>) map.getCastOrNull("channels", List.class);
            if (channels == null) {
                channels = new ArrayList<>();
                map.put("channels", channels);
                guildData.save();
            }

            return channels.contains(key.getId());

        });

    }

    @SuppressWarnings("unchecked")
    public void setChannelAllowed(@NotNull TextChannel channel, boolean value) {

        checkValid();

        IGuildData guildData = repository.createGuildData(channel.getGuild());
        TypeMap map = guildData.container().getOrCreate(key);

        List<String> channels = (List<String>) map.getCastOrDefault("channels", List.class, new ArrayList<>());

        allowedChannels.put(channel, value);

        if (value) channels.add(channel.getId());
        else channels.remove(channel.getId());

        guildData.save();

    }

}
