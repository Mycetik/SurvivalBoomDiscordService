package net.survivalboom.sbds.api.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IGuildDataManager extends IManager {

    @NotNull ISBDS getSbds();

    // CREATE //

    @NotNull CompletableFuture<@NotNull IGuildData> create(long id);

    default @NotNull CompletableFuture<@NotNull IGuildData> create(@NotNull Guild guild) {
        return create(guild.getIdLong());
    }

    // DELETE //

    @NotNull CompletableFuture<Void> delete(long id);

    default @NotNull CompletableFuture<Void> delete(@NotNull Guild guild) {
        return delete(guild.getIdLong());
    }

    default @NotNull CompletableFuture<Void> delete(@NotNull IGuildData guild) {
        return delete(guild.getGuild().getIdLong());
    }

    // GET //

    @NotNull CompletableFuture<@Nullable IGuildData> get(long id);

    default @NotNull CompletableFuture<@Nullable IGuildData> get(@NotNull Guild guild) {
        return get(guild.getIdLong());
    }

    // OBTAIN //

    @NotNull CompletableFuture<@NotNull IGuildData> obtain(long id);

    default @NotNull CompletableFuture<@NotNull IGuildData> obtain(@NotNull Guild guild) {
        return obtain(guild.getIdLong());
    }

    // SAVE //

    void save(@NotNull IGuildData guildData);


}
