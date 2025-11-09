package net.survivalboom.sbds.api.database.users;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface IUserRepositoryHandler {

    @NotNull CompletableFuture<IUserData> getUser(@NotNull User user);

    @NotNull CompletableFuture<IUserData> getUser(long id);


    @NotNull CompletableFuture<IUserData> createUser(@NotNull User user);

    @NotNull CompletableFuture<IUserData> createUser(long id);


    @NotNull CompletableFuture<Void> deleteUser(@NotNull User user);

    @NotNull CompletableFuture<Void> deleteUser(@NotNull IUserData userData);

    @NotNull CompletableFuture<Void> deleteUser(long id);

}
