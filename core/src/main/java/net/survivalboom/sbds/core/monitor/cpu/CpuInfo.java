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

import net.survivalboom.sbds.api.monitoring.cpu.ICpuInfo;
import net.survivalboom.sbds.core.monitor.utils.LinuxProc;
import net.survivalboom.sbds.core.monitor.utils.MacosSysctl;
import net.survivalboom.sbds.core.monitor.utils.WindowsWmic;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Small utility to query the CPU model on Linux and Windows systems.
 */
public class CpuInfo implements ICpuInfo {

    private static final Pattern SPACE_COLON_SPACE_PATTERN = Pattern.compile("\\s+:\\s");

    private String cpuModel = null;

    public void query() {
        this.cpuModel = queryCpuModel();
    }

    public @Nullable String model() {
        return cpuModel;
    }

    /**
     * Queries the CPU model.
     *
     * @return the cpu model
     */
    private @Nullable String queryCpuModel() {
        for (String line : LinuxProc.CPUINFO.read()) {
            String[] splitLine = SPACE_COLON_SPACE_PATTERN.split(line);
            if (splitLine[0].equals("model name") || splitLine[0].equals("Processor")) {
                return splitLine[1];
            }
        }

        for (String line : WindowsWmic.CPU_GET_NAME.read()) {
            if (line.startsWith("Name")) {
                return line.substring(5).trim();
            }
        }

        for (String line : MacosSysctl.SYSCTL.read()) {
            if (line.startsWith("machdep.cpu.brand_string:")) {
                return line.substring("machdep.cpu.brand_string:".length()).trim();
            }
        }

        return null;
    }

}
