package net.survivalboom.sbds.modules.chatbot.storage;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
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

public class BannedUsers extends Manager {

    private final IDatabase database;


    private final Map<User, Boolean> bannedUsers = new HashMap<>();

    private final NamespacedKey key;


    private IGuildRepositoryHandler repository;


    public BannedUsers(@NotNull ModuleMain module) {
        this.database = module.getDatabase();
        this.key = NamespacedKey.fromModule(module, "banned_users");
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
    public boolean isUserBanned(@NotNull Guild guild, @NotNull User user) {

        checkValid();

        return bannedUsers.computeIfAbsent(user, key -> {

            IGuildData guildData = repository.createGuildData(guild);
            TypeMap map = guildData.container().getOrCreate(this.key);
            List<String> users = (List<String>) map.getCastOrNull("users", List.class);
            if (users == null) {
                users = new ArrayList<>();
                map.put("users", users);
                guildData.save();
            }

            return users.contains(key.getId());

        });

    }

    @SuppressWarnings("unchecked")
    public void setUserAllowed(@NotNull Guild guild, @NotNull User user, boolean value) {

        checkValid();

        IGuildData guildData = repository.createGuildData(guild);
        TypeMap map = guildData.container().getOrCreate(key);

        List<String> users = (List<String>) map.getCastOrDefault("users", List.class, new ArrayList<>());

        bannedUsers.put(user, value);

        if (value) users.add(user.getId());
        else users.remove(user.getId());

        guildData.save();

    }

}
