package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.utils.ConcurrentWeakHashMap;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class RepositoryHandler<T extends DataRecord> {

    protected final Map<Long, T> cache = new ConcurrentWeakHashMap<>();

    protected final Class<T> dataRecordClass;

    protected IRepository repository;


    public RepositoryHandler(@NotNull Class<T> clazz) {
        this.dataRecordClass = clazz;
    }



    public void configure(@NotNull IRepository repository) {
        this.repository = repository;
    }

    public void reload() {
        purgeCache();
    }

    //
    // SESSION
    //

    @SuppressWarnings("unchecked")
    protected <V> CompletableFuture<V> sessionReturn(@NotNull Function<Session, V> function, boolean cache) {

        Objects.requireNonNull(function, "function == null");

        return repository.queueSessionRequest(function).thenApply(result -> {

            if (!cache) {
                return result;
            }

            if (result instanceof Collection<?> collection) {

                var dataRecords = (Collection<T>) collection;
                var map = dataRecords.stream().collect(Collectors.toMap(DataRecord::getId, Function.identity()));

                this.cache.putAll(map);

            }

            else {

                T cast;
                try {
                    cast = (T) result;
                }

                catch (ClassCastException e) {
                    throw new IllegalArgumentException(
                            "Hibernate returned `" + result + "` instead of required by this repository handler (`" + dataRecordClass.getName() + "`) object type."
                                    + "Looks like you wrote incorrect database query"
                    );
                }

                if (cast != null) {
                    this.cache.put(cast.getId(), cast);
                }

            }

            return result;

        });

    }

    protected @NotNull CompletableFuture<Void> session(@NotNull Consumer<Session> consumer) {
        return repository.queueSessionRequest(consumer);
    }

    protected @NotNull Session getSession() {
        return repository.getSession();
    }

    //
    // DATA
    //

    protected @NotNull CompletableFuture<T> save(@NotNull T record) {
        Objects.requireNonNull(record, "record == null");
        return sessionReturn(session -> session.merge(record), true).thenApply(v -> cache.put(record.getId(), record));
    }

    protected @NotNull CompletableFuture<Void> delete(@NotNull T record) {

        Objects.requireNonNull(record, "record == null");

        return session(session -> session.remove(record))
                .thenAccept(v -> cache.remove(record.getId()));

    }

    protected @NotNull CompletableFuture<Void> delete(long id) {

        return getById(id).thenCompose(record -> {

            if (record == null) {
                return CompletableFuture.completedFuture(null);
            }

            return session(session -> session.remove(record));

        });

    }

    public @NotNull CompletableFuture<@Nullable T> getById(long id) {

        if (!cache.containsKey(id)) {
            return sessionReturn(session -> session.get(dataRecordClass, id), false)
                    .thenApply(v -> {
                        cache.put(id, v);
                        return v;
                    });
        }

        return CompletableFuture.completedFuture(cache.get(id));

    }

    //
    // GETTERS
    //

    public void purgeCache() {
        this.cache.clear();
    }

    public @NotNull Map<Long, T> getCache() {
        return new HashMap<>(cache);
    }

    public @NotNull IRepository getRepository() {
        return repository;
    }

    public @NotNull Class<T> getDataRecordClass() {
        return dataRecordClass;
    }

}
