package net.survivalboom.sbds.core.database.users;

import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UserRepositoryHandler extends RepositoryHandler<UserData> implements IUserRepositoryHandler {

    @Override
    public void checkTables() throws SQLException {

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS sbds_userdata (id bigint, translation text, data jsonb)");
        }

    }

    @Override
    public @Nullable IUserData getUser(long id, boolean updateCache) {
        return null;
    }

    @Override
    public @NotNull IUserData createUser(long id) {
        return null;
    }

    @Override
    public void deleteUser(long id) {

    }
}
