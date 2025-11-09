package net.survivalboom.sbds.moderation.module.moderation;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.moderation.module.storage.Punishment;
import net.survivalboom.sbds.moderation.module.storage.PunishmentRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ExpiringQueue extends Manager {

    private static final Logger log = LoggerFactory.getLogger(ExpiringQueue.class);
    private final ModuleMain main;

    private final ModerationManager manager;

    private final DelayQueue<Expiring> queue = new DelayQueue<>();

    private ISchedulerTask expiringHandlerTask = null;


    public ExpiringQueue(@NotNull ModerationManager manager, @NotNull ModuleMain main) {
        this.main = main;
        this.manager = manager;
    }


    @Override
    protected void init0() {

        expiringHandlerTask = main.getSbds().getScheduler().schedule(main, "ModerationManager-ExpiringHandlerTask", this::expiringHandlerTask, 0, 1000);

    }

    @Override
    protected void shutdown0() {

        queue.clear();
        expiringHandlerTask.cancelForce();
        expiringHandlerTask = null;

    }


    public void queue(@NotNull PunishmentRepositoryHandler<?> repository, long id, long time) {

        checkValid();

        if (time < 0) {
            throw new IllegalArgumentException("time < 0");
        }

        this.queue.add(new Expiring(repository, id, time));

    }

    public void queue(@NotNull PunishmentRepositoryHandler<?> repository, @NotNull Punishment punishment) {

        checkValid();

        Instant end = punishment.getEnd();
        if (end == null) {
            throw new IllegalArgumentException("Punishment has no expiry time");
        }

        this.queue.add(new Expiring(repository, punishment.getId(), end.getEpochSecond()));

    }


    private void expiringHandlerTask() {

        Expiring next;
        try {
            next = queue.take();
        }

        catch (InterruptedException ignored) {
            return;
        }

        Punishment punishment = next.repository.getById(next.id).join();
        Objects.requireNonNull(punishment, "punishment == null! suka blyat! шо за пиздець блять???");

        log.info("Punishment `{}` was expired! Removing...", punishment);
        manager.removePunishment(punishment, null, null, null).join();

    }


    static class Expiring implements Delayed {

        private final long id;

        private final long endTimestamp;

        private final PunishmentRepositoryHandler<?> repository;


        public Expiring(@NotNull PunishmentRepositoryHandler<?> repository, long id, long endTimestamp) {
            this.id = id;
            this.endTimestamp = endTimestamp;
            this.repository = repository;
        }


        @Override
        public long getDelay(@NotNull TimeUnit timeUnit) {

            var currentTimeMillis = System.currentTimeMillis();
            var currentTimeSeconds = currentTimeMillis / 1000;

            var sourceTime = currentTimeSeconds - endTimestamp;

            var t = timeUnit.convert(sourceTime, TimeUnit.SECONDS);

            return t;

        }

        @Override
        public int compareTo(@NotNull Delayed delayed) {
            return Long.compare(this.endTimestamp, ((Expiring) delayed).endTimestamp);
        }

    }


}
