package net.survivalboom.sbds.modules.moderation.module.utils;

import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.modules.moderation.module.ModerationModule;
import net.survivalboom.sbds.modules.moderation.module.storage.Punishment;
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
    private final ModerationModule main;

    private final DelayQueue<Expiring> queue = new DelayQueue<>();

    private ISchedulerTask expiringHandlerTask = null;


    public ExpiringQueue(@NotNull ModerationModule main) {
        this.main = main;
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


    public void queue(@NotNull ExpiringModerationManager<?> manager, long id, long time) {

        checkValid();

        if (time < 0) {
            throw new IllegalArgumentException("time < 0");
        }

        this.queue.add(new Expiring(manager, id, time));

    }

    public void queue(@NotNull ExpiringModerationManager<?> manager, @NotNull Punishment punishment) {

        checkValid();

        Instant end = punishment.getEnd();
        if (end == null) {
            throw new IllegalArgumentException("Punishment has no expiry time");
        }

        this.queue.add(new Expiring(manager, punishment.getId(), end.getEpochSecond()));

    }

    public void remove(@NotNull Punishment punishment, @NotNull ExpiringModerationManager<?> manager) {
        this.queue.removeIf(e -> e.id == punishment.getId() && e.manager.equals(manager));
    }


    private void expiringHandlerTask() {

        Expiring next;
        try {
            next = queue.take();
        }

        catch (InterruptedException ignored) {
            return;
        }

        Punishment punishment = next.manager.getById0(null, next.id).join();
        Objects.requireNonNull(punishment, "punishment == null! suka blyat! шо за пиздець блять???");

        log.info("Punishment `{}` was expired! Removing...", punishment);
        main.removePunishment(punishment, null, null, null).join();

    }


    public static class Expiring implements Delayed {

        private final long id;

        private final long endTimestamp;

        private final ExpiringModerationManager<?> manager;


        public Expiring(@NotNull ExpiringModerationManager<?> manager, long id, long endTimestamp) {
            this.id = id;
            this.endTimestamp = endTimestamp;
            this.manager = manager;
        }


        @Override
        public long getDelay(@NotNull TimeUnit timeUnit) {

            var currentTimeMillis = System.currentTimeMillis();
            var currentTimeSeconds = currentTimeMillis / 1000;

            var sourceTime = endTimestamp - currentTimeSeconds;

            return timeUnit.convert(sourceTime, TimeUnit.SECONDS);

        }

        @Override
        public int compareTo(@NotNull Delayed delayed) {
            return Long.compare(this.endTimestamp, ((Expiring) delayed).endTimestamp);
        }


        public long getId() {
            return id;
        }

        public long getEndTimestamp() {
            return endTimestamp;
        }

    }


}
