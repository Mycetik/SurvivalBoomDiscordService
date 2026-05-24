package net.survivalboom.sbds.core.database;

import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class DatabaseQueue extends Manager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseQueue.class);

    private final Database database;

    private final Scheduler scheduler;

    private final Queue<PendingQuery<?>> sessionRequestsQueue = new ConcurrentLinkedQueue<>();

    private final Map<DataRecord, CompletableFuture<Void>> recordsSavingQueue = new ConcurrentHashMap<>();

    private SchedulerTask task;


    public DatabaseQueue(@NotNull Database database, @NotNull Scheduler scheduler) {
        this.database = database;
        this.scheduler = scheduler;
    }


    @Override
    protected void init0() {
        task = scheduler.schedule0(null, "database_queue", task -> pushAll(), 0, 150);
    }

    @Override
    protected void shutdown0() {

        task.tryCancel();
        task = null;

        if (!sessionRequestsQueue.isEmpty() || !recordsSavingQueue.isEmpty()) {
            log.warn("There is {} database queries in queue. Blocking the thread and executing them all...", sessionRequestsQueue.size() + recordsSavingQueue.size());
            pushAll();
        }

    }


    @SuppressWarnings("unchecked")
    private void pushAll() {

        if (sessionRequestsQueue.isEmpty()) {
            return;
        }

        try (Session session = database.createSession()) {

            Transaction transaction = session.beginTransaction();

            while (!sessionRequestsQueue.isEmpty()) {

                var request = sessionRequestsQueue.poll();

                var function = (Function<Session, Object>) request.function;
                var future = (CompletableFuture<Object>) request.future;

                try {

                    var result = function.apply(session);
                    future.complete(result);

                    log.info("PUSH! {}", result);

                }

                catch (Throwable t) {
                    log.error("An error occurred while trying to execute database query. Looks like something just break. This may cause data loss.", t);
                    future.completeExceptionally(t);
                }

            }

            for (var entry : recordsSavingQueue.entrySet()) {

                var record = entry.getKey();
                var future = entry.getValue();

                try {
                    session.merge(record);
                    future.complete(null);
                }

                catch (Throwable t) {
                    future.completeExceptionally(t);
                    log.error("An error occurred while attempting to save a record `{}`. This may cause data loss.", record, t);
                }

                finally {
                    recordsSavingQueue.remove(record);
                }

            }

            transaction.commit();

        }

    }

    public @NotNull <V> CompletableFuture<V> queueSessionRequest(@NotNull Function<Session, V> function) {

        Objects.requireNonNull(function, "function == null");

        CompletableFuture<V> future = new CompletableFuture<>();
        sessionRequestsQueue.add(new PendingQuery<>(function, future));

        return future;

    }

    public @NotNull CompletableFuture<Void> queueSessionRequest(@NotNull Consumer<Session> consumer) {

        Objects.requireNonNull(consumer, "consumer == null");

        CompletableFuture<Void> future = new CompletableFuture<>();
        sessionRequestsQueue.add(new PendingQuery<>(session -> this.gibiskus(consumer, session), future));

        return future;

    }

    // ЫЫЫЫЫ КАКАЯ ХУЕТА ЫЫЫЫЫЫ, А МНЕ ПАХУЮ!!!! ХУЕТА!!!! ЫАЫАЫАЫАЫАЫА!!!! //
    private Void gibiskus(@NotNull Consumer<Session> consumer, @NotNull Session session) {
        consumer.accept(session);
        return null;
    }

    public @NotNull CompletableFuture<Void> queueRecordSave(@NotNull DataRecord record) {
        return recordsSavingQueue.computeIfAbsent(record, k -> new CompletableFuture<>());
    }

    private record PendingQuery<V>(
            @NotNull Function<Session, V> function,
            @NotNull CompletableFuture<V> future
    ) {}

}
