/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.survivalboom.sbds.core.monitor.memory;

import net.survivalboom.sbds.api.monitoring.memory.IMemoryInfo;
import net.survivalboom.sbds.core.monitor.utils.LinuxProc;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to query information about system memory usage.
 */
public class MemoryInfo implements IMemoryInfo {

    /** The OperatingSystemMXBean instance */
    private final OperatingSystemMXBean BEAN;

    /** The format used by entries in /proc/meminfo */
    private final Pattern PROC_MEMINFO_VALUE = Pattern.compile("^(\\w+):\\s*(\\d+) kB$");

    public MemoryInfo() {

         try {

             MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();

             /** The object name of the com.sun.management.OperatingSystemMXBean */
             String OPERATING_SYSTEM_BEAN = "java.lang:type=OperatingSystem";

             ObjectName diagnosticBeanName = ObjectName.getInstance(OPERATING_SYSTEM_BEAN);
             BEAN = JMX.newMXBeanProxy(beanServer, diagnosticBeanName, OperatingSystemMXBean.class);

         }

         catch (Exception e) {
             throw new UnsupportedOperationException("OperatingSystemMXBean is not supported by the system", e);
         }

    }

    /* Swap */

    @Override
    public long getUsedSwap() {
        return BEAN.getTotalSwapSpaceSize() - BEAN.getFreeSwapSpaceSize();
    }

    @Override
    public long getTotalSwap() {
        return BEAN.getTotalSwapSpaceSize();
    }

    /* Physical Memory */

    @Override
    public long getUsedPhysicalMemory() {
        return getTotalPhysicalMemory() - getAvailablePhysicalMemory();
    }

    @Override
    public long getTotalPhysicalMemory() {
        // try to read from /proc/meminfo on linux systems
        for (String line : LinuxProc.MEMINFO.read()) {
            Matcher matcher = PROC_MEMINFO_VALUE.matcher(line);
            if (matcher.matches()) {
                String label = matcher.group(1);
                long value = Long.parseLong(matcher.group(2)) * 1024; // kB -> B

                if (label.equals("MemTotal")) {
                    return value;
                }
            }
        }

        // fallback to JVM measure
        return BEAN.getTotalPhysicalMemorySize();
    }

    @Override
    public long getAvailablePhysicalMemory() {
        boolean present = false;
        long free = 0, buffers = 0, cached = 0, sReclaimable = 0;

        for (String line : LinuxProc.MEMINFO.read()) {
            Matcher matcher = PROC_MEMINFO_VALUE.matcher(line);
            if (matcher.matches()) {
                present = true;

                String label = matcher.group(1);
                long value = Long.parseLong(matcher.group(2)) * 1024; // kB -> B

                // if MemAvailable is set, just return that
                if (label.equals("MemAvailable")) {
                    return value;
                }

                // otherwise, record MemFree, Buffers, Cached and SReclaimable
                switch (label) {
                    case "MemFree":
                        free = value;
                        break;
                    case "Buffers":
                        buffers = value;
                        break;
                    case "Cached":
                        cached = value;
                        break;
                    case "SReclaimable":
                        sReclaimable = value;
                        break;
                }
            }
        }

        // estimate how much is "available" - not exact but this is probably good enough.
        // most Linux systems (assuming they have been updated in the last ~8 years) will
        // have MemAvailable set, and we return that instead if present
        //
        // useful ref: https://www.linuxatemyram.com/
        if (present) {
            return free + buffers + cached + sReclaimable;
        }

        // fallback to what the JVM understands as "free"
        // on non-linux systems, this is probably good enough to estimate what's available
        return BEAN.getFreePhysicalMemorySize();
    }

    /* Virtual Memory */

    @Override
    public long getTotalVirtualMemory() {
        return BEAN.getCommittedVirtualMemorySize();
    }

    public interface OperatingSystemMXBean {
        long getCommittedVirtualMemorySize();
        long getTotalSwapSpaceSize();
        long getFreeSwapSpaceSize();
        long getFreePhysicalMemorySize();
        long getTotalPhysicalMemorySize();
    }

}
