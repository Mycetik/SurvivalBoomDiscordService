package net.survivalboom.sbds.core.database;

import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseSaveQueue extends Manager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSaveQueue.class.getSimpleName());

    private final Map<DataRecord, CompletableFuture<Void>> queue = new ConcurrentHashMap<>();

    private final Database database;

    private final SBDS sbds;

    private SchedulerTask task;


    public DatabaseSaveQueue(@NotNull Database database, @NotNull SBDS sbds) {
        this.database = database;
        this.sbds = sbds;
    }


    @Override
    protected void init0() {
        this.task = sbds.getScheduler().schedule0(null, "SBDS-DatabaseSaveQueue", task -> tick(), 1000, 1000);
    }

    @Override
    protected void shutdown0() {

        task.cancelAndWait(1000, true);

        if (!queue.isEmpty()) {
            log.warn("There is {} hibernate entities in queue. Saving...", queue.size());
            saveAll();
        }

    }


    private void tick() {
        saveAll();
    }

    private void saveAll() {

        if (queue.isEmpty()) {
            return;
        }

        try (Session session = database.createSession()) {

            Transaction transaction = session.beginTransaction();
            for (var entry : queue.entrySet()) {

                DataRecord record = entry.getKey();
                CompletableFuture<Void> future = entry.getValue();

                database.checkRepository(record);

                try {
                    session.merge(record);
                    future.complete(null);
                }

                catch (Throwable t) {
                    log.error("An error occurred while trying to save an entity {}. Looks like you break something. This may cause data loss.", record, t);
                    future.completeExceptionally(t);
                }

                queue.remove(record);

            }

            transaction.commit();

        }

    }


    public synchronized @NotNull CompletableFuture<Void> queue(@NotNull DataRecord dataRecord) {
        return queue.computeIfAbsent(dataRecord, k -> new CompletableFuture<>());
    }

    public int size() {
        return queue.size();
    }

}
