package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IRepository<T extends DataRecord> extends IValid {

    //
    // REPOSITORY
    //

    @NotNull IDatabase getDatabase();

    @NotNull Registration<IRepository<T>> getRegistration();

    @NotNull Class<T> getRecordClass();

    //
    // DATABASE QUERIES
    //

    // SESSIONS //

    @NotNull Session requestSession();

    @NotNull <V> CompletableFuture<V> queueSessionReturnRequest(@NotNull Function<Session, V> function);

    @NotNull CompletableFuture<Void> queueSessionRequest(@NotNull Consumer<Session> consumer);

    // OPERATIONS //

    @NotNull CompletableFuture<T> saveRecord(@NotNull T record);

    @NotNull CompletableFuture<Void> deleteRecord(@NotNull T record);

    @NotNull CompletableFuture<Void> deleteRecord(long id);

    @NotNull CompletableFuture<@Nullable T> getRecordById(long id);

}
