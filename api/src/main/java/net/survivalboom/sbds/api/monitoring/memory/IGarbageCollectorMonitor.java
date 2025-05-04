package net.survivalboom.sbds.api.monitoring.memory;

import com.sun.management.GarbageCollectionNotificationInfo;
import org.jetbrains.annotations.NotNull;

public interface IGarbageCollectorMonitor {

    /**
     * A simple listener object for garbage collections.
     */
    interface Listener {
        void onGc(GarbageCollectionNotificationInfo data);
    }


    void addListener(@NotNull Listener listener);

    void removeListener(@NotNull Listener listener);

    /**
     * Gets a human-friendly description for the type of the given GC notification.
     *
     * @param info the notification object
     * @return the name of the GC type
     */
    static String getGcType(GarbageCollectionNotificationInfo info) {

        if (info.getGcAction().equals("end of minor GC")) {
            return "Young Gen";
        }

        else if (info.getGcAction().equals("end of major GC")) {
            return "Old Gen";
        }

        else {
            return info.getGcAction();
        }

    }

}
