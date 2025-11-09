package net.survivalboom.sbds.core.database.users;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class UserRepositoryHandler extends RepositoryHandler<UserData> implements IUserRepositoryHandler {

    public UserRepositoryHandler() {
        super(UserData.class);
    }


    @Override
    public @NotNull CompletableFuture<@Nullable IUserData> getUser(@NotNull User user) {
        return getUser(user.getIdLong());
    }

    @Override
    public @NotNull CompletableFuture<@Nullable IUserData> getUser(long id) {
        return getById(id).thenApply(d -> d);
    }

    @Override
    public @NotNull CompletableFuture<IUserData> createUser(@NotNull User user) {
        return createUser(user.getIdLong());
    }

    @Override
    public @NotNull CompletableFuture<IUserData> createUser(long id) {

        return getUser(id).thenCompose(v -> {

            if (v != null) {
                return CompletableFuture.completedFuture(v);
            }

            return save(new UserData(id)).thenApply(d -> d);

        });

    }

    @Override
    public @NotNull CompletableFuture<Void> deleteUser(@NotNull User user) {
        return deleteUser(user.getIdLong());
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteUser(@NotNull IUserData userData) {
        UserData ud = (UserData) userData;
        return delete(ud);
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteUser(long id) {
        return delete(id);
    }

}
