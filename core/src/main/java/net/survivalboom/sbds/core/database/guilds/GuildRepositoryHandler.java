package net.survivalboom.sbds.core.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildRepositoryHandler extends RepositoryHandler<GuildData> implements IGuildRepositoryHandler {

    public GuildRepositoryHandler() {
        super(GuildData.class);
    }

    @Override
    public @Nullable GuildData getGuildData(long id) {

        GuildData guildData = cache.get(id);
        if (guildData == null) {

            guildData = sessionReturn(session -> session.get(GuildData.class, id));
            if (guildData == null) return null;

            cache.put(id, guildData);

        }

        return guildData;

    }

    @Override
    public @Nullable GuildData getGuildData(@NotNull Guild guild) {
        return getGuildData(guild.getIdLong());
    }


    @Override
    public @NotNull IGuildData createGuildData(long id) {

        GuildData guildData = getGuildData(id);
        if (guildData != null) return guildData;

        guildData = new GuildData(id);
        save(guildData);

        cache.put(id, guildData);

        return guildData;

    }

    @Override
    public @NotNull IGuildData createGuildData(@NotNull Guild guild) {
        return createGuildData(guild.getIdLong());
    }


    @Override
    public boolean deleteGuildData(long id) {

        GuildData guildData = getGuildData(id);
        if (guildData == null) return false;

        delete(guildData);

        return true;

    }

    @Override
    public boolean deleteGuildData(@NotNull Guild guild) {
        return deleteGuildData(guild.getIdLong());
    }

}
