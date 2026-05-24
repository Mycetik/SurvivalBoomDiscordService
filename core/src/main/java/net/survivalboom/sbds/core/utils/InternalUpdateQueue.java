package net.survivalboom.sbds.core.utils;

import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class InternalUpdateQueue extends Manager {

    private final Scheduler scheduler;

    private final String name;

    private final Runnable runnable;

    private final Logger log;


    private final int delay;

    private ISchedulerTask task;

    private boolean updateRequested = false;

    private long lastAppend = 0;


    public InternalUpdateQueue(
            @NotNull Runnable runnable,
            @NotNull String name,
            int delay,
            @NotNull Scheduler scheduler
    ) {

        Objects.requireNonNull(runnable, "runnable == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(scheduler, "scheduler == null");

        this.runnable = runnable;
        this.name = name;
        this.delay = delay;
        this.scheduler = scheduler;

        this.log = LoggerFactory.getLogger(name + "-UpdateQueue");

    }


    @Override
    protected void init0() {
        task = scheduler.schedule0(null, name, task -> task(), 1000, 500);
    }

    @Override
    protected void shutdown0() {
        task.tryCancel();
        task = null;
    }


    private void task() {

        if (!updateRequested) {
            return;
        }

        long time = System.currentTimeMillis();
        if (lastAppend + delay > time) {
            return;
        }

        this.updateRequested = false;

        try {
            runnable.run();
        }

        catch (Throwable t) {
            log.error("Failed to execute an update.", t);
        }

    }

    public void requestUpdate() {
        checkValid();
        this.updateRequested = true;
        this.lastAppend = System.currentTimeMillis();
    }

}
