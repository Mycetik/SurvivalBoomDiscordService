package net.survivalboom.sbds.core.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GuildRepositoryHandler extends RepositoryHandler<GuildDataRecord> implements IGuildRepositoryHandler {

    public GuildRepositoryHandler() {
        super(GuildDataRecord.class);
    }

    @Override
    public @NotNull CompletableFuture<@Nullable IGuildData> getGuildData(long id) {
        return getById(id).thenApply(d -> d);
    }

    @Override
    public @NotNull CompletableFuture<@Nullable IGuildData> getGuildData(@NotNull Guild guild) {
        return getGuildData(guild.getIdLong());
    }


    @Override
    public @NotNull CompletableFuture<IGuildData> createGuildData(long id) {

        return getGuildData(id).thenCompose(gd -> {

            if (gd != null) {
                return CompletableFuture.completedFuture(gd);
            }

            return save(new GuildDataRecord(id)).thenApply(d -> d);

        });

    }

    @Override
    public @NotNull CompletableFuture<IGuildData> createGuildData(@NotNull Guild guild) {
        return createGuildData(guild.getIdLong());
    }


    @Override
    public CompletableFuture<Void> deleteGuildData(long id) {
        return delete(id);
    }

    @Override
    public CompletableFuture<Void> deleteGuildData(@NotNull Guild guild) {
        return deleteGuildData(guild.getIdLong());
    }

}
