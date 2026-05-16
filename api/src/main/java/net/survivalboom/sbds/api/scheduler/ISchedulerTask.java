package net.survivalboom.sbds.api.scheduler;

import net.survivalboom.sbds.api.registrations.Registration;
import org.jetbrains.annotations.NotNull;

public interface ISchedulerTask {

    @NotNull Registration<ISchedulerTask> getRegistration();

    //
    // LIFECYCLE
    //

    void cancel();

    void kill();

    boolean cancelAndWaitOrKill(int timeout, boolean log);

    default boolean tryCancel() {
        return cancelAndWaitOrKill(5000, true);
    }



    void waitForCancel(int timeout);

    default void waitForCancel() {
        waitForCancel(30000);
    }

    //
    // PROPERTIES
    //

    int getDelay();

    int getPeriod();


    boolean isRunning();

    boolean isCancelled();

    boolean isStopped();


    @NotNull Thread getThread();

}
