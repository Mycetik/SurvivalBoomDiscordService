package net.survivalboom.sbds.core.database;

import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.database.IRepository;
import net.survivalboom.sbds.core.modules.Module;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class Repository extends Valid implements IRepository {

    private final Database database;

    private RepositoryHandler<?> handler;


    private final NamespacedKey name;

    private final Module module;

    private final Logger logger;


    public Repository(@Nullable Module module, @NotNull NamespacedKey name, @NotNull Database database) {
        this.module = module;
        this.name = name;
        this.database = database;
        this.logger = LoggerFactory.getLogger("Repository-" + name.prefix() + "-" + name.key());
    }

    public void configure(@NotNull RepositoryHandler<?> handler) {
        if (this.handler != null) throw new RuntimeException("already configured, debil4ik!");
        this.handler = handler;
        handler.configure(this);
    }

    public void reload() {

        try {
            handler.reload();
        }

        catch (Throwable t) {
            logger.error("Failed to reload repository properly. This may cause bugs, memory leaks, exceptions, and data corruption.", t);
        }

    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return database.requestConnection(this);
    }

    @Override
    public @NotNull NamespacedKey getName() {
        return name;
    }

    @Override
    public @NotNull String getNameRaw() {
        return name.toString();
    }

    @Override
    public @Nullable Module getModule() {
        return module;
    }

    @Override
    public @NotNull RepositoryHandler<?> getHandler() {
        return handler;
    }

    @Override
    public @NotNull IDatabase getDatabase() {
        return database;
    }

    public void invalid() {
        valid(false);
    }

}
