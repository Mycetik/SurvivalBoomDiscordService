package net.survivalboom.sbds.core.scheduler;

import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.core.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class SchedulerTask implements ISchedulerTask {

    private final Scheduler scheduler;

    private final String name;

    private final Module module;

    private final Consumer<ISchedulerTask> consumer;

    private final Logger logger;


    private final int delay;

    private final int period;


    private Thread thread;


    private boolean run = true;

    private boolean stopped = false;



    public SchedulerTask(@Nullable Module module, @NotNull Scheduler scheduler, @NotNull String name, @NotNull Consumer<ISchedulerTask> consumer, int delay, int period) {
        this.name = name;
        this.module = module;
        this.scheduler = scheduler;
        this.consumer = consumer;
        this.logger = scheduler.getLogger();
        this.delay = delay;
        this.period = period;
    }

    public void launch() {

        if (thread != null || stopped) throw new IllegalStateException("Already launched");

        thread = Thread.startVirtualThread(this::run);
        thread.setName(name);

    }

    private void run() {

        // Даем задержку перед запуском задачи, проверяя отмену
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < delay) {

            // Если задача отменена, сразу выходим
            if (!run) {
                stop();
                return;
            }

            CommonUtils.sleep(100); // Проверяем флаг отмены каждую секунду

        }

        // Если период не задан (меньше или равен 0), сразу выполняем задачу один раз
        if (period <= 0) {
            execute();
            stop();
            return;
        }

        // Запускаем цикл, пока задача не отменена
        while (run) {

            execute(); // Выполняем задачу

            if (!run) break; // Проверяем, была ли отменена задача, и если да, то выходим из цикла

            // Для немедленной отмены нужно использовать проверку флага внутри sleep
            startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < period) {

                if (!run) break; // Если задача отменена, прерываем цикл

                // Прерываем сон, если задача отменена
                CommonUtils.sleep(100); // Проверяем флаг каждую секунду

            }

        }

        // Завершаем выполнение задачи
        stop();

    }

    private void execute() {

        try {
            consumer.accept(this);
        }

        catch (Throwable t) {
            logger.error("An exception was thrown in task {}.", name, t);
            CommonUtils.sleep(1000);
        }

    }

    private void stop() {
        run = false;
        stopped = true;
        thread = null;
        scheduler.unregisterTask(this);
    }

    @Override
    public void cancel() {
        if (stopped) return;
        run = false;
    }

    @Override
    public void cancelForce() {

        if (stopped) return;

        thread.interrupt();

        stop();

    }

    @Override
    public void cancelAndWait() {
        cancelAndWait(-1, false);
    }

    @Override
    public boolean cancelAndWait(int timeout, boolean force, @Nullable Runnable onKill) {

        if (stopped) return false;

        cancel();

        try {
            waitForCancel(timeout);
        }

        catch (RuntimeException e) {
            if (!force) throw e;
            if (onKill != null) onKill.run();
            cancelForce();
            return true;
        }

        return false;

    }

    @Override
    public boolean cancelAndWait(int timeout, boolean force) {
        return cancelAndWait(timeout, force, null);
    }

    @Override
    public void waitForCancel() {
        waitForCancel(-1);
    }

    @Override
    public void waitForCancel(int timeout) {
        if (stopped) return;
        CommonUtils.waitUntil(this::isStopped, timeout);
    }


    @Override
    public boolean isRunning() {
        return !stopped;
    }

    @Override
    public boolean isCancelled() {
        return stopped || !run;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @Nullable Module getModule() {
        return module;
    }


    @Override
    public @NotNull Thread getThread() {
        if (thread == null) throw new IllegalStateException("Task is not running");
        return thread;
    }

}
