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

package net.survivalboom.sbds.core.monitor.cpu;


import net.survivalboom.sbds.api.monitoring.cpu.ICpuMonitor;
import net.survivalboom.sbds.api.monitoring.utils.RollingAverage;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.jetbrains.annotations.NotNull;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;

/**
 * Exposes and monitors the system/process CPU usage.
 */
public class CpuMonitor implements ICpuMonitor {

    /** The OperatingSystemMXBean instance */
    private final OperatingSystemMXBean BEAN;

    private final Scheduler scheduler;

    // Rolling averages for system/process data
    private final RollingAverage SYSTEM_AVERAGE_10_SEC = new RollingAverage(10);
    private final RollingAverage SYSTEM_AVERAGE_1_MIN = new RollingAverage(60);
    private final RollingAverage SYSTEM_AVERAGE_15_MIN = new RollingAverage(60 * 15);
    private final RollingAverage PROCESS_AVERAGE_10_SEC = new RollingAverage(10);
    private final RollingAverage PROCESS_AVERAGE_1_MIN = new RollingAverage(60);
    private final RollingAverage PROCESS_AVERAGE_15_MIN = new RollingAverage(60 * 15);


    private final RollingAverage[] systemAverages = new RollingAverage[]{
            SYSTEM_AVERAGE_10_SEC,
            SYSTEM_AVERAGE_1_MIN,
            SYSTEM_AVERAGE_15_MIN
    };

    private final RollingAverage[] processAverages = new RollingAverage[]{
            PROCESS_AVERAGE_10_SEC,
            PROCESS_AVERAGE_1_MIN,
            PROCESS_AVERAGE_15_MIN
    };


    private SchedulerTask monitoringTask = null;


    public CpuMonitor(@NotNull Scheduler scheduler) {
        this.scheduler = scheduler;

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

    public void startMonitoringTask() {
        // schedule rolling average calculations.
        monitoringTask = scheduler.schedule0(null, "CpuMonitor", task -> this.run(), 1000, 1000);
    }

    public void stopMonitoringTask() {
        monitoringTask.cancelAndWait(1000, true);
    }


    @Override
    public double systemLoad() {
        return BEAN.getSystemCpuLoad();
    }

    @Override
    public double systemLoad10SecAvg() {
        return SYSTEM_AVERAGE_10_SEC.mean();
    }

    @Override
    public double systemLoad1MinAvg() {
        return SYSTEM_AVERAGE_1_MIN.mean();
    }

    @Override
    public double systemLoad15MinAvg() {
        return SYSTEM_AVERAGE_15_MIN.mean();
    }

    @Override
    public double processLoad() {
        return BEAN.getProcessCpuLoad();
    }

    @Override
    public double processLoad10SecAvg() {
        return PROCESS_AVERAGE_10_SEC.mean();
    }

    @Override
    public double processLoad1MinAvg() {
        return PROCESS_AVERAGE_1_MIN.mean();
    }

    @Override
    public double processLoad15MinAvg() {
        return PROCESS_AVERAGE_15_MIN.mean();
    }


    private void run() {

        BigDecimal systemCpuLoad = BigDecimal.valueOf(systemLoad());
        BigDecimal processCpuLoad = BigDecimal.valueOf(processLoad());

        if (systemCpuLoad.signum() != -1) { // if value is not negative
            for (RollingAverage average : this.systemAverages) {
                average.add(systemCpuLoad);
            }
        }

        if (processCpuLoad.signum() != -1) { // if value is not negative
            for (RollingAverage average : this.processAverages) {
                average.add(processCpuLoad);
            }
        }

    }


    public interface OperatingSystemMXBean {
        double getSystemCpuLoad();
        double getProcessCpuLoad();
    }

}
