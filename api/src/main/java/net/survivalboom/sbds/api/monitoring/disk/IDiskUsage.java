package net.survivalboom.sbds.api.monitoring.disk;

/**
 * Exposes the system disk usage.
 */
public interface IDiskUsage {

    long getUsed();

    long getTotal();

}
