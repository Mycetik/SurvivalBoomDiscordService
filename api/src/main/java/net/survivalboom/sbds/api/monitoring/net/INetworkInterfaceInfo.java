package net.survivalboom.sbds.api.monitoring.net;

import org.jetbrains.annotations.NotNull;

public interface INetworkInterfaceInfo {

    @NotNull String getName();


    long getReceivedBytes();

    long getReceivedPackets();

    long getReceiveErrors();


    long getTransmittedBytes();

    long getTransmittedPackets();

    long getTransmitErrors();


    long getBytes(Direction direction);

    long getPackets(Direction direction);


    boolean isZero();

}
