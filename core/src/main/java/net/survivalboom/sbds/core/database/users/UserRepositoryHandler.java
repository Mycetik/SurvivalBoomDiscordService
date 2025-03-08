package net.survivalboom.sbds.core.database.users;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.database.users.IUserRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserRepositoryHandler extends RepositoryHandler<UserData> implements IUserRepositoryHandler {

    public UserRepositoryHandler() {
        super(UserData.class);
    }

    @Override
    public @Nullable UserData getUser(@NotNull User user) {
        return getUser(user.getIdLong());
    }

    @Override
    public @Nullable UserData getUser(long id) {

        UserData userData = cache.get(id);
        if (userData == null) {

            userData = sessionReturn(session -> session.get(UserData.class, id));

            if (userData == null) return null;

            cache.put(id, userData);
        }

        return userData;


    }

    @Override
    public @NotNull UserData createUser(@NotNull User user) {
        return createUser(user.getIdLong());
    }

    @Override
    public @NotNull UserData createUser(long id) {

        UserData iuserData = getUser(id);
        if (iuserData != null) return iuserData;

        UserData userData = new UserData(id);

        save(userData);

        cache.put(id, userData);

        return userData;

    }

    @Override
    public boolean deleteUser(@NotNull User user) {
        return deleteUser(user.getIdLong());
    }

    @Override
    public boolean deleteUser(long id) {

        UserData userData = getUser(id);
        if (userData == null) return false;

        delete(userData);

        return true;

    }

}
