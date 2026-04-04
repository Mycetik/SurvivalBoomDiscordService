package net.survivalboom.sbds.core.scheduler;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.scheduler.IScheduler;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.modules.Module;
import net.survivalboom.sbds.api.utils.valid.Manager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.*;
import java.util.function.Consumer;

public class Scheduler extends Manager implements IScheduler {

    private final SBDS sbds;

    private final Random random = new Random();

    private final Set<SchedulerTask> tasks = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger("Scheduler");

    public Scheduler(@NotNull SBDS sbds) {
        this.sbds = sbds;
    }

    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {

        List<SchedulerTask> tasks = getTasks0();
        if (tasks.isEmpty()) return;

        logger.info("Stopping tasks.");

        for (SchedulerTask task : tasks) {

            boolean killed = task.cancelAndWait(5000, true, () -> {
                logger.warn("- Killed {}...", task.getName());
                CommonUtils.logThreadStackTrace(logger, Level.WARN, task.getThread());
            });

            if (killed) continue;

            logger.info("- Stopped {}...", task.getName());

        }

    }


    //
    // TASK CREATION
    //

    public @NotNull SchedulerTask schedule0(@Nullable IModule imodule, @Nullable String name, @NotNull Consumer<ISchedulerTask> consumer, int delay, int period) {

        checkValid();

        Objects.requireNonNull(consumer, "consumer == null");

        String taskName = createName(name);
        SchedulerTask task;
        if (imodule != null) {
            Module module = sbds.getModuleManager().checkModuleEnabled(imodule, "Disabled module attempted to register a task");
            task = new SchedulerTask(module, this, taskName, consumer, delay, period);
            module.getRegistration().add("Scheduler:" + task.getName(), task::cancel);
        }

        else task = new SchedulerTask(null, this, taskName, consumer, delay, period);

        tasks.add(task);

        task.launch();

        return task;

    }

    @Override
    public @NotNull SchedulerTask schedule(@NotNull IModule imodule, @Nullable String name, @NotNull Consumer<ISchedulerTask> consumer, int delay, int period) {
        Objects.requireNonNull(imodule, "module == null");
        return schedule0(imodule, name, consumer, delay, period);
    }

    @Override
    public @NotNull SchedulerTask schedule(@NotNull IModule module, @NotNull Consumer<ISchedulerTask> consumer, int delay, int period) {
        return schedule(module, null, consumer, delay, period);
    }


    @Override
    public @NotNull SchedulerTask schedule(@NotNull IModule module, @Nullable String name, @NotNull Runnable runnable, int delay, int period) {
        return schedule(module, name, task -> runnable.run(), delay, period);
    }

    @Override
    public @NotNull SchedulerTask schedule(@NotNull IModule module, @NotNull Runnable runnable, int delay, int period) {
        return schedule(module, null, runnable, delay, period);
    }


    @Override
    public @NotNull List<ISchedulerTask> getTasks() {
        return new ArrayList<>(tasks);
    }

    public @NotNull List<SchedulerTask> getTasks0() {
        return new ArrayList<>(tasks);
    }


    //
    // MISC
    //

    public void cancelAll(@NotNull Module module) {
        getTasks0().stream().filter(task -> module.equals(task.getModule())).forEach(SchedulerTask::cancel);
    }

    public void unregisterTask(@NotNull SchedulerTask task) {
        tasks.remove(task);
    }

    private @NotNull String createName(@Nullable String name) {

        int number = random.nextInt(9999);

        if (name == null) name = "Task";

        return name + "-" + number;

    }


    public @NotNull Logger getLogger() {
        return logger;
    }

}
