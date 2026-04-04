package net.survivalboom.sbds.api.utils.valid;

import org.jetbrains.annotations.NotNull;

public interface IManager {

    default @NotNull String getManagerName() {
        return getClass().getSimpleName();
    }

}
