package net.survivalboom.sbds.api.database.users;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IUserRepositoryHandler {

    default @Nullable IUserData getUser(@NotNull User user) {
        return getUser(user.getIdLong());
    }

    @Nullable IUserData getUser(long id);


    default @NotNull IUserData createUser(@NotNull User user) {
        return createUser(user.getIdLong());
    }

    @NotNull IUserData createUser(long id);


    default boolean deleteUser(@NotNull User user) {
        return deleteUser(user.getIdLong());
    }

    boolean deleteUser(long id);

}
