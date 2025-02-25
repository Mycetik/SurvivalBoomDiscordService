package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public interface IRepository {

    @NotNull Connection getConnection() throws SQLException;


    @NotNull NamespacedKey getName();

    @NotNull String getNameRaw();

    @Nullable IModule getModule();

    @NotNull RepositoryHandler getHandler();

    @NotNull IDatabase getDatabase();

}
