package net.survivalboom.sbds.api.monitoring;

import net.survivalboom.sbds.api.monitoring.cpu.ICpuInfo;
import net.survivalboom.sbds.api.monitoring.cpu.ICpuMonitor;
import net.survivalboom.sbds.api.monitoring.disk.IDiskUsage;
import net.survivalboom.sbds.api.monitoring.memory.IGarbageCollectorMonitor;
import net.survivalboom.sbds.api.monitoring.memory.IMemoryInfo;
import net.survivalboom.sbds.api.monitoring.net.INetworkMonitor;
import net.survivalboom.sbds.api.monitoring.os.IOperatingSystemInfo;
import org.jetbrains.annotations.NotNull;

public interface ISystemMonitor {

    @NotNull ICpuInfo getCpuInfo();

    @NotNull ICpuMonitor getCpuMonitor();


    @NotNull IDiskUsage getDiskUsage();


    @NotNull IGarbageCollectorMonitor getGarbageCollectionMonitor();

    @NotNull IMemoryInfo getMemoryInfo();


    @NotNull IOperatingSystemInfo getOperatingSystemInfo();

    @NotNull INetworkMonitor getNetworkMonitor();

}
