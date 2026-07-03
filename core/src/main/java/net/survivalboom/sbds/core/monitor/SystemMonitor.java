package net.survivalboom.sbds.core.monitor;

import net.survivalboom.sbds.api.monitoring.ISystemMonitor;
import net.survivalboom.sbds.api.monitoring.disk.IDiskUsage;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.monitor.cpu.CpuInfo;
import net.survivalboom.sbds.core.monitor.cpu.CpuMonitor;
import net.survivalboom.sbds.core.monitor.disk.DiskUsage;
import net.survivalboom.sbds.core.monitor.memory.GarbageCollectorMonitor;
import net.survivalboom.sbds.core.monitor.memory.MemoryInfo;
import net.survivalboom.sbds.core.monitor.net.NetworkMonitor;
import net.survivalboom.sbds.core.monitor.os.OperatingSystemInfo;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemMonitor extends Manager implements ISystemMonitor {


    private static final Logger log = LoggerFactory.getLogger("SystemMonitor");

    private final CpuInfo cpuInfo = new CpuInfo();

    private final CpuMonitor cpuMonitor;


    private final DiskUsage diskUsage = new DiskUsage();


    private final GarbageCollectorMonitor garbageCollectionMonitor = new GarbageCollectorMonitor();

    private final MemoryInfo memoryInfo = new MemoryInfo();


    private final OperatingSystemInfo operatingSystemInfo = OperatingSystemInfo.create();

    private final NetworkMonitor networkMonitor;


    public SystemMonitor(@NotNull Scheduler scheduler) {
        this.cpuMonitor = new CpuMonitor(scheduler);
        this.networkMonitor = new NetworkMonitor(scheduler);
    }

    @Override
    protected void init0() {

        cpuInfo.query();
        cpuMonitor.startMonitoringTask();
        networkMonitor.startMonitorTask();

    }

    @Override
    protected void shutdown0() {

        cpuMonitor.stopMonitoringTask();
        networkMonitor.stopMonitorTask();

    }


    @Override
    public @NotNull CpuInfo getCpuInfo() {
        checkValid();
        return cpuInfo;
    }

    @Override
    public @NotNull CpuMonitor getCpuMonitor() {
        checkValid();
        return cpuMonitor;
    }

    @Override
    public @NotNull IDiskUsage getDiskUsage() {
        checkValid();
        return diskUsage;
    }

    @Override
    public @NotNull GarbageCollectorMonitor getGarbageCollectionMonitor() {
        checkValid();
        return garbageCollectionMonitor;
    }

    @Override
    public @NotNull MemoryInfo getMemoryInfo() {
        checkValid();
        return memoryInfo;
    }

    @Override
    public @NotNull OperatingSystemInfo getOperatingSystemInfo() {
        checkValid();
        return operatingSystemInfo;
    }

    @Override
    public @NotNull NetworkMonitor getNetworkMonitor() {
        return networkMonitor;
    }

}
