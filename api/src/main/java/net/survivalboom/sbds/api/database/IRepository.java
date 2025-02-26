package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public interface IRepository {

    @NotNull Session getSession();


    @NotNull NamespacedKey getName();

    @NotNull String getNameRaw();

    @Nullable IModule getModule();

    @NotNull RepositoryHandler<? extends DataRecord> getHandler();

    @NotNull IDatabase getDatabase();

}
