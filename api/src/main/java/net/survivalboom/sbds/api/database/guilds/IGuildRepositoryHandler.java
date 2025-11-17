package net.survivalboom.sbds.api.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IGuildRepositoryHandler {

    @NotNull CompletableFuture<@Nullable IGuildData> getGuildData(long id);

    @NotNull CompletableFuture<@Nullable IGuildData> getGuildData(@NotNull Guild guild);


    @NotNull CompletableFuture<IGuildData> createGuildData(long id);

    @NotNull CompletableFuture<IGuildData> createGuildData(@NotNull Guild guild);


    CompletableFuture<Void> deleteGuildData(long id);

    CompletableFuture<Void> deleteGuildData(@NotNull Guild guild);


}
