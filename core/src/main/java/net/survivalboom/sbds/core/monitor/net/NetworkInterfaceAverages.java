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

import net.survivalboom.sbds.api.monitoring.net.Direction;
import net.survivalboom.sbds.api.monitoring.net.INetworkAverages;
import net.survivalboom.sbds.api.monitoring.utils.RollingAverage;

import java.math.BigDecimal;

public final class NetworkInterfaceAverages implements INetworkAverages {

    private final RollingAverage rxBytesPerSecond;
    private final RollingAverage txBytesPerSecond;
    private final RollingAverage rxPacketsPerSecond;
    private final RollingAverage txPacketsPerSecond;

    NetworkInterfaceAverages(int windowSize) {
        this.rxBytesPerSecond = new RollingAverage(windowSize);
        this.txBytesPerSecond = new RollingAverage(windowSize);
        this.rxPacketsPerSecond = new RollingAverage(windowSize);
        this.txPacketsPerSecond = new RollingAverage(windowSize);
    }

    void accept(NetworkInterfaceInfo info, RateCalculator rateCalculator) {
        this.rxBytesPerSecond.add(rateCalculator.calculate(info.getReceivedBytes()));
        this.txBytesPerSecond.add(rateCalculator.calculate(info.getTransmittedBytes()));
        this.rxPacketsPerSecond.add(rateCalculator.calculate(info.getReceivedPackets()));
        this.txPacketsPerSecond.add(rateCalculator.calculate(info.getTransmittedPackets()));
    }

    interface RateCalculator {
        BigDecimal calculate(long value);
    }

    @Override
    public RollingAverage bytesPerSecond(Direction direction) {
        return switch (direction) {
            case RECEIVE -> rxBytesPerSecond();
            case TRANSMIT -> txBytesPerSecond();
        };
    }

    @Override
    public RollingAverage packetsPerSecond(Direction direction) {
        return switch (direction) {
            case RECEIVE -> rxPacketsPerSecond();
            case TRANSMIT -> txPacketsPerSecond();
        };
    }

    @Override
    public RollingAverage rxBytesPerSecond() {
        return this.rxBytesPerSecond;
    }

    @Override
    public RollingAverage rxPacketsPerSecond() {
        return this.rxPacketsPerSecond;
    }

    @Override
    public RollingAverage txBytesPerSecond() {
        return this.txBytesPerSecond;
    }

    @Override
    public RollingAverage txPacketsPerSecond() {
        return this.txPacketsPerSecond;
    }

}
