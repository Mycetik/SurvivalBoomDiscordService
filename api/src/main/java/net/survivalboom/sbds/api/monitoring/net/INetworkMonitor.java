package net.survivalboom.sbds.api.monitoring.net;

import java.util.Map;

public interface INetworkMonitor {

    Map<String, INetworkInterfaceInfo> systemTotals();

    Map<String, INetworkAverages> systemAverages();

}
