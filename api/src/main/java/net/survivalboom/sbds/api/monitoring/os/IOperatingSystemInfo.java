package net.survivalboom.sbds.api.monitoring.os;

import org.jetbrains.annotations.NotNull;

public interface IOperatingSystemInfo {

    @NotNull String name();

    @NotNull String version();

    @NotNull String arch();


    @NotNull String fullName();

}
