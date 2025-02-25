package net.survivalboom.sbds.api.scheduler;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface IScheduler {

    @NotNull ISchedulerTask schedule(@NotNull IModule module, @Nullable String name, @NotNull Consumer<ISchedulerTask> consumer, int delay, int period);

    @NotNull ISchedulerTask schedule(@NotNull IModule module, @NotNull Consumer<ISchedulerTask> consumer, int delay, int period);


    @NotNull ISchedulerTask schedule(@NotNull IModule module, @Nullable String name, @NotNull Runnable runnable, int delay, int period);

    @NotNull ISchedulerTask schedule(@NotNull IModule module, @NotNull Runnable runnable, int delay, int period);


    default @NotNull ISchedulerTask schedule(@NotNull ModuleMain main, @Nullable String name, @NotNull Consumer<ISchedulerTask> consumer, int delay, int period) {
        return schedule(main.getModule(), name, consumer, delay, period);
    }

    default @NotNull ISchedulerTask schedule(@NotNull ModuleMain main, @NotNull Consumer<ISchedulerTask> consumer, int delay, int period) {
        return schedule(main.getModule(), consumer, delay, period);
    }


    default @NotNull ISchedulerTask schedule(@NotNull ModuleMain main, @Nullable String name, @NotNull Runnable runnable, int delay, int period) {
        return schedule(main.getModule(), name, runnable, delay, period);
    }

    default @NotNull ISchedulerTask schedule(@NotNull ModuleMain main, @NotNull Runnable runnable, int delay, int period) {
        return schedule(main.getModule(), runnable, delay, period);
    }


    @NotNull List<ISchedulerTask> getTasks();

}
