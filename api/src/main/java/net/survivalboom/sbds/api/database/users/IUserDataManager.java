package net.survivalboom.sbds.api.database.users;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IUserDataManager extends IManager {

    @NotNull ISBDS getSbds();

    // CREATE //

    @NotNull CompletableFuture<@NotNull IUserData> create(long id);

    default @NotNull CompletableFuture<@NotNull IUserData> create(@NotNull User user) {
        return create(user.getIdLong());
    }

    // DELETE //

    @NotNull CompletableFuture<Void> delete(long id);

    default @NotNull CompletableFuture<Void> delete(@NotNull IUserData userData) {
        return delete(userData.getUser().getIdLong());
    }

    default @NotNull CompletableFuture<Void> delete(@NotNull User user) {
        return delete(user.getIdLong());
    }

    // GET //

    @NotNull CompletableFuture<@Nullable IUserData> get(long id);

    default @NotNull CompletableFuture<@Nullable IUserData> get(@NotNull User user) {
        return get(user.getIdLong());
    }

    // OBTAIN //

    @NotNull CompletableFuture<@NotNull IUserData> obtain(long id);

    default @NotNull CompletableFuture<@NotNull IUserData> obtain(@NotNull User user) {
        return obtain(user.getIdLong());
    }

    // SAVE //

    void save(@NotNull IUserData user);

}
