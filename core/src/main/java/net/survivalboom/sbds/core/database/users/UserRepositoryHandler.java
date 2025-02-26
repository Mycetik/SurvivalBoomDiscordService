package net.survivalboom.sbds.core.database.users;

import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserRepositoryHandler extends RepositoryHandler<UserData> implements IUserRepositoryHandler {

    public UserRepositoryHandler() {
        super(UserData.class);
    }

    @Override
    public @Nullable IUserData getUser(long id) {

        UserData userData = cache.get(id);
        if (userData == null) {

            userData = sessionReturn(session -> session.get(UserData.class, id));

            if (userData == null) return null;

            cache.put(id, userData);
        }

        return userData;


    }

    @Override
    public @NotNull IUserData createUser(long id) {

        IUserData iuserData = getUser(id);
        if (iuserData != null) return iuserData;

        UserData userData = new UserData(id);

        save(userData);

        cache.put(id, userData);

        return userData;

    }

    @Override
    public boolean deleteUser(long id) {

        UserData userData = (UserData) getUser(id);
        if (userData == null) return false;

        delete(userData);

        return true;

    }

}
