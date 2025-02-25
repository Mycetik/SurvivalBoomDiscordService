package net.survivalboom.sbds.api.scheduler;

import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISchedulerTask {

    void cancel();

    void cancelForce();

    void cancelAndWait();

    boolean cancelAndWait(int timeout, boolean force, @NotNull Runnable onKill);

    boolean cancelAndWait(int timeout, boolean force);

    void waitForCancel();

    void waitForCancel(int timeout);


    boolean isRunning();

    boolean isCancelled();

    boolean isStopped();

    @NotNull String getName();

    @Nullable IModule getModule();


    @NotNull Thread getThread();

}
