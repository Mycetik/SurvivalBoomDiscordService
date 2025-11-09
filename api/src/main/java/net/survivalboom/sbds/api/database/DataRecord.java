package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.SbdsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class DataRecord {

    public void save() {
        save(this);
    }

    public abstract long getId();

    //
    // STATIC
    //

    public static void save(@NotNull DataRecord record) {
        SbdsProvider.getInstance().getDatabase().queueSave(record);
    }


    public static long hash(Object... args) {
        return Math.abs(Objects.hash(args));
    }

}
