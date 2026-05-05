package net.survivalboom.sbds.api.database.users;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IUserDataManager extends IManager {

    // CREATE //

    @NotNull CompletableFuture<@NotNull IUserData> create(@NotNull User user);

    @NotNull CompletableFuture<@NotNull IUserData> create(long id);

    // DELETE //

    @NotNull CompletableFuture<Void> delete(@NotNull IUserData userData);

    @NotNull CompletableFuture<Void> delete(@NotNull User user);

    @NotNull CompletableFuture<Void> delete(long id);

    // GET //

    @NotNull CompletableFuture<@Nullable IUserData> get(@NotNull User user);

    @NotNull CompletableFuture<@Nullable IUserData> get(long id);

    // OBTAIN //

    @NotNull CompletableFuture<@NotNull IUserData> obtain(@NotNull User user);

    @NotNull CompletableFuture<@NotNull IUserData> obtain(long id);

}
