package net.survivalboom.sbds.core.database;

import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class DatabaseSaveQueue extends Manager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSaveQueue.class.getSimpleName());

    private final Set<EntityToSave> queue = new HashSet<>();

    private final Database database;

    private final SBDS sbds;

    private SchedulerTask task;


    public DatabaseSaveQueue(@NotNull Database database, @NotNull SBDS sbds) {
        this.database = database;
        this.sbds = sbds;
    }


    @Override
    protected void init0() {
        this.task = sbds.getScheduler().schedule0(null, "DatabaseSaveQueue", task -> tick(), 1000, 1000);
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

        if (queue.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        try (Session session = database.createSession()) {

            Transaction transaction = session.beginTransaction();
            for (EntityToSave entity : new ArrayList<>(queue)) {

                if (entity.time.get() + 5000 >= currentTime) {
                    break;
                }

                try {
                    session.merge(entity.record);
                }

                catch (Throwable t) {

                    log.error("An error occurred while trying to save an entity {}. Looks like you break something. This may cause data loss.", entity.record.getClass().getSimpleName(), t);

                }

                queue.remove(entity);

            }

            transaction.commit();

        }

    }


    public void queue(@NotNull DataRecord dataRecord) {

        EntityToSave entity = queue.stream()
                .filter(e -> e.record.equals(dataRecord))
                .findAny()
                .orElse(null);

        long currentTime = System.currentTimeMillis();

        if (entity != null) {
            entity.time.set(currentTime);
            return;
        }

        entity = new EntityToSave(dataRecord, new AtomicLong(currentTime));
        queue.add(entity);

    }

    public int size() {
        return queue.size();
    }


    record EntityToSave(@NotNull DataRecord record, @NotNull AtomicLong time) {}

}
