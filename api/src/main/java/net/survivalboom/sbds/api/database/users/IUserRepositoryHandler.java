package net.survivalboom.sbds.api.database.users;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IUserRepositoryHandler {

    @Nullable IUserData getUser(@NotNull User user);

    @Nullable IUserData getUser(long id);


    @NotNull IUserData createUser(@NotNull User user);

    @NotNull IUserData createUser(long id);


    boolean deleteUser(@NotNull User user);

    boolean deleteUser(long id);

}
