package net.survivalboom.sbds.api.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IGuildDataManager extends IManager {

    // CREATE //

    @NotNull CompletableFuture<@NotNull IGuildData> create(long id);

    @NotNull CompletableFuture<@NotNull IGuildData> create(@NotNull Guild guild);

    // DELETE //

    @NotNull CompletableFuture<Void> delete(@NotNull IGuildData guild);

    default @NotNull CompletableFuture<Void> delete(@NotNull Guild guild) {

        return get(guild).thenCompose(data -> {

            if (data == null) {
                return CompletableFuture.completedFuture(null);
            }

            return delete(data);

        });

    }

    default @NotNull CompletableFuture<Void> delete(long id) {

        return get(id).thenCompose(data -> {

            if (data == null) {
                return CompletableFuture.completedFuture(null);
            }

            return delete(data);

        });

    }

    // GET //

    @NotNull CompletableFuture<@Nullable IGuildData> get(long id);

    @NotNull CompletableFuture<@Nullable IGuildData> get(@NotNull Guild guild);

    // OBTAIN //

    default @NotNull CompletableFuture<@NotNull IGuildData> obtain(long id) {

        return get(id).thenCompose(data -> {

            if (data != null) {
                return CompletableFuture.completedFuture(data);
            }

            return create(id);

        });

    }

    default @NotNull CompletableFuture<@NotNull IGuildData> obtain(@NotNull Guild guild) {

        return get(guild).thenCompose(data -> {

            if (data != null) {
                return CompletableFuture.completedFuture(data);
            }

            return create(guild);

        });

    }


}
