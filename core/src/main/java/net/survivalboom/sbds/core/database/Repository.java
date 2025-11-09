package net.survivalboom.sbds.core.database;

import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.database.IRepository;
import net.survivalboom.sbds.core.modules.Module;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Valid;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

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

    //
    // DATABASE QUERIES
    //

    @Override
    public @NotNull Session getSession() {
        return database.requestSession(this);
    }

    @Override
    public @NotNull <V> CompletableFuture<V> queueSessionRequest(@NotNull Function<Session, V> function) {
        return database.queueSessionRequest(this, function);
    }

    @Override
    public @NotNull CompletableFuture<Void> queueSessionRequest(@NotNull Consumer<Session> consumer) {
        return database.queueSessionRequest(this, consumer);
    }

    //
    // GETTERS
    //

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
    public @NotNull RepositoryHandler<? extends DataRecord> getHandler() {
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
