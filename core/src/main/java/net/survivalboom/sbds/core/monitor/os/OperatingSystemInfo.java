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

package net.survivalboom.sbds.core.monitor.os;

import net.survivalboom.sbds.api.monitoring.os.IOperatingSystemInfo;
import net.survivalboom.sbds.core.monitor.utils.LinuxProc;
import net.survivalboom.sbds.core.monitor.utils.WindowsWmic;
import org.jetbrains.annotations.NotNull;

/**
 * Small utility to query the operating system name & version.
 */
public record OperatingSystemInfo(@NotNull String name, @NotNull String version, @NotNull String arch) implements IOperatingSystemInfo {

    @Override
    public @NotNull String fullName() {
        return name + " " + version + " " + arch;
    }


    public static @NotNull OperatingSystemInfo create() {

        String name = null;
        String version = null;

        for (String line : LinuxProc.OSINFO.read()) {
            if (line.startsWith("PRETTY_NAME") && line.length() > 13) {
                name = line.substring(13).replace('"', ' ').trim();
            }
        }

        for (String line : WindowsWmic.OS_GET_CAPTION_AND_VERSION.read()) {
            if (line.startsWith("Caption") && line.length() > 18) {
                // Caption=Microsoft Windows something
                // \----------------/ = 18 chars
                name = line.substring(18).trim();
            } else if (line.startsWith("Version")) {
                // Version=10.0.something
                // \------/ = 8 chars
                version = line.substring(8).trim();
            }
        }

        if (name == null) {
            name = System.getProperty("os.name");
        }

        if (version == null) {
            version = System.getProperty("os.version");
        }

        String arch = System.getProperty("os.arch");

        return new OperatingSystemInfo(name, version, arch);

    }

}
