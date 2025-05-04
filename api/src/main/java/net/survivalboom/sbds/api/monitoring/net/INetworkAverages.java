package net.survivalboom.sbds.api.monitoring.net;

import net.survivalboom.sbds.api.monitoring.utils.RollingAverage;

public interface INetworkAverages {

    RollingAverage bytesPerSecond(Direction direction);

    RollingAverage packetsPerSecond(Direction direction);


    RollingAverage rxBytesPerSecond();

    RollingAverage rxPacketsPerSecond();


    RollingAverage txBytesPerSecond();

    RollingAverage txPacketsPerSecond();

}
