package net.survivalboom.sbds.core.database.users;

import net.survivalboom.sbds.api.database.users.IUserDataManager;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.database.Database;
import org.jetbrains.annotations.NotNull;

public class UserDataManager extends Manager implements IUserDataManager {

    private final Database database;


    public UserDataManager(@NotNull Database database) {
        this.database = database;
    }

    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {

    }

}
