package net.survivalboom.sbds.api;

import org.jetbrains.annotations.Nullable;

public class SbdsProvider {

    private static ISBDS sbds;

    public static void internal_internal_internal_internal_internal_internal_set(@Nullable ISBDS s) {
        sbds = s;
    }

    public static ISBDS getInstance() {
        return sbds;
    }

}
