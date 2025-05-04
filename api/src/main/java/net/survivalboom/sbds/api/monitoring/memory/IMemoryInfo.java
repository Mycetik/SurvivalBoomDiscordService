package net.survivalboom.sbds.api.monitoring.memory;

public interface IMemoryInfo {

    long getUsedSwap();

    long getTotalSwap();


    long getTotalPhysicalMemory();

    long getUsedPhysicalMemory();

    long getAvailablePhysicalMemory();


    long getTotalVirtualMemory();

}
