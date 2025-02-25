package net.survivalboom.sbds.api.database.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IUserRepositoryHandler {

    @Nullable IUserData getUser(long id, boolean updateCache);

    @NotNull IUserData createUser(long id);

    void deleteUser(long id);

}
