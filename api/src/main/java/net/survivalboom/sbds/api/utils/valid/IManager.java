package net.survivalboom.sbds.api.utils.valid;

import org.jetbrains.annotations.NotNull;

public interface IManager extends IValid {

    default @NotNull String getManagerName() {
        return getClass().getSimpleName();
    }

}
