package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IRepository {

    //
    // DATABASE QUERIES
    //

    @NotNull Session getSession();

    @NotNull <V> CompletableFuture<V> queueSessionRequest(@NotNull Function<Session, V> function);

    @NotNull CompletableFuture<Void> queueSessionRequest(@NotNull Consumer<Session> consumer);


    //
    // GETTERS
    //

    @NotNull NamespacedKey getName();

    @NotNull String getNameRaw();

    @Nullable IModule getModule();

    @NotNull RepositoryHandler<? extends DataRecord> getHandler();

    @NotNull IDatabase getDatabase();



}
