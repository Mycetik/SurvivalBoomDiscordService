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

package net.survivalboom.sbds.core.monitor.net;

import net.survivalboom.sbds.api.monitoring.net.INetworkAverages;
import net.survivalboom.sbds.api.monitoring.net.INetworkInterfaceInfo;
import net.survivalboom.sbds.api.monitoring.net.INetworkMonitor;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Exposes and monitors the system/process network usage.
 */
public class NetworkMonitor implements INetworkMonitor {

    private final Scheduler scheduler;

    private SchedulerTask monitorTask = null;

    // Latest readings
    private final AtomicReference<Map<String, NetworkInterfaceInfo>> SYSTEM = new AtomicReference<>();
    
    // a pattern to match the interface names to exclude from monitoring
    // ignore: virtual eth adapters + container bridge networks
    private final Pattern INTERFACES_TO_IGNORE = Pattern.compile("^(veth\\w+)|(br-\\w+)$");

    // Rolling averages for system/process data over 15 mins
    private final Map<String, NetworkInterfaceAverages> SYSTEM_AVERAGES = new ConcurrentHashMap<>();
    
    // poll every minute, keep rolling averages for 15 mins
    private final int POLL_INTERVAL = 60;
    private final BigDecimal POLL_INTERVAL_DECIMAL = BigDecimal.valueOf(POLL_INTERVAL);
    private final int WINDOW_SIZE_SECONDS = (int) TimeUnit.MINUTES.toSeconds(15);
    private final int WINDOW_SIZE = WINDOW_SIZE_SECONDS / POLL_INTERVAL; // 15


    public NetworkMonitor(@NotNull Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    public void startMonitorTask() {
        monitorTask = scheduler.schedule0(null, "NetworkMonitor", task -> this.run(), 1000, 1000);
    }

    public void stopMonitorTask() {
        monitorTask.cancelAndWait(1000, true);
        monitorTask = null;
    }

    public Map<String, NetworkInterfaceInfo> systemTotals0() {
        Map<String, NetworkInterfaceInfo> values = SYSTEM.get();
        return values == null ? Collections.emptyMap() : values;
    }

    public Map<String, NetworkInterfaceAverages> systemAverages0() {
        return Collections.unmodifiableMap(SYSTEM_AVERAGES);
    }


    @Override
    public Map<String, INetworkInterfaceInfo> systemTotals() {
        return new HashMap<>(systemTotals0());
    }

    @Override
    public Map<String, INetworkAverages> systemAverages() {
        return new HashMap<>(systemAverages0());
    }



    /**
     * Task to poll network activity and add to the rolling averages in the enclosing class.
     */



    private void run() {
        Map<String, NetworkInterfaceInfo> values = pollAndDiff(NetworkInterfaceInfo::pollSystem, SYSTEM);
        if (values != null) {
            submit(SYSTEM_AVERAGES, values);
        }
    }

    /**
     * Submits the incoming values into the rolling averages map.
     *
     * @param values the values
     */
    private void submit(Map<String, NetworkInterfaceAverages> rollingAveragesMap, Map<String, NetworkInterfaceInfo> values) {
        // ensure all incoming keys are present in the rolling averages map
        for (String key : values.keySet()) {
            if (!INTERFACES_TO_IGNORE.matcher(key).matches()) {
                rollingAveragesMap.computeIfAbsent(key, k -> new NetworkInterfaceAverages(WINDOW_SIZE));
            }
        }

        // submit a value (0 if unknown) to each rolling average instance in the map
        for (Map.Entry<String, NetworkInterfaceAverages> entry : rollingAveragesMap.entrySet()) {
            String interfaceName = entry.getKey();
            NetworkInterfaceAverages rollingAvgs = entry.getValue();

            NetworkInterfaceInfo info = values.getOrDefault(interfaceName, NetworkInterfaceInfo.ZERO);
            rollingAvgs.accept(info, this::calculateRate);
        }
    }

    private BigDecimal calculateRate(long value) {
        return BigDecimal.valueOf(value).divide(POLL_INTERVAL_DECIMAL, RoundingMode.HALF_UP);
    }

    private Map<String, NetworkInterfaceInfo> pollAndDiff(Supplier<Map<String, NetworkInterfaceInfo>> poller, AtomicReference<Map<String, NetworkInterfaceInfo>> valueReference) {
        // poll the latest value from the supplier
        Map<String, NetworkInterfaceInfo> latest = poller.get();

        // update the value ref.
        // if the previous value was null, and the new value is empty, keep it null
        Map<String, NetworkInterfaceInfo> previous = valueReference.getAndUpdate(prev -> {
            if (prev == null && latest.isEmpty()) {
                return null;
            } else {
                return latest;
            }
        });

        if (previous == null) {
            return null;
        }

        return NetworkInterfaceInfo.difference(latest, previous);
    }

}
