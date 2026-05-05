package net.survivalboom.sbds.core.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildDataManager;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.database.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GuildDataManager extends Manager implements IGuildDataManager {

    private final Database database;


    public GuildDataManager(@NotNull Database database) {
        this.database = database;
    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {

    }

    //
    // GUILD DATA
    //

    // CREATE //

    @Override
    public @NotNull CompletableFuture<IGuildData> create(@NotNull Guild guild) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<IGuildData> create(long id) {
        return null;
    }

    // DELETE //

    @Override
    public @NotNull CompletableFuture<Void> delete(@NotNull IGuildData guild) {
        return null;
    }

    // GET //

    @Override
    public @NotNull CompletableFuture<@Nullable IGuildData> get(@NotNull Guild guild) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<@Nullable IGuildData> get(long id) {
        return null;
    }

}
