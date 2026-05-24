package net.survivalboom.sbds.core.utils;

import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public class InternalOperationQueue<obj> extends Manager {

    private final Scheduler scheduler;

    private final String name;

    private final Consumer<InternalOperationQueue<obj>> consumer;

    private final Logger log;


    private final int delay;

    private ISchedulerTask task;

    private final Map<String, Set<obj>> queue = new HashMap<>();

    private long lastAppend = 0;



    public InternalOperationQueue(
            @NotNull Consumer<InternalOperationQueue<obj>> consumer,
            @NotNull String name,
            int delay,
            @NotNull Scheduler scheduler
    ) {

        Objects.requireNonNull(consumer, "consumer == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(scheduler, "scheduler == null");

        this.consumer = consumer;
        this.scheduler = scheduler;
        this.name = name;
        this.delay = delay;

        this.log = LoggerFactory.getLogger(name + "-PushQueue");

    }

    public InternalOperationQueue(
            @NotNull Consumer<InternalOperationQueue<obj>> consumer,
            @NotNull String name,
            int delay,
            @NotNull SBDS sbds
    ) {
        this(consumer, name, delay, sbds.getScheduler());
    }

    //
    // MANAGER
    //

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

        if (queue.isEmpty()) {
            return;
        }

        long time = System.currentTimeMillis();
        if (lastAppend + delay > time) {
            return;
        }

        try {
            consumer.accept(this);
        }

        catch (Throwable t) {
            log.error("Failed to execute an update on {} objects in the queue!", queue.size(), t);
        }

        this.queue.clear();

    }

    //
    // QUEUE
    //

    public void append(@NotNull String operation, @NotNull obj obj) {

        Objects.requireNonNull(obj, "obj == null");
        Objects.requireNonNull(operation, "operation == null");
        checkValid();

        this.queue.computeIfAbsent(operation, k -> new HashSet<>()).add(obj);
        this.lastAppend = System.currentTimeMillis();

    }

    public @NotNull Map<String, Set<obj>> getQueue() {
        checkValid();
        return CommonUtils.deepCopy(this.queue);
    }

}
