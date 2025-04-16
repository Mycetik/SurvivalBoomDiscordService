package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.SbdsProvider;
import net.survivalboom.sbds.api.utils.Valid;
import org.jetbrains.annotations.NotNull;

public abstract class DataRecord extends Valid {

    public void invalid() {
        valid(false);
    }

    public void save() {
        save(this);
    }

    public static void save(@NotNull DataRecord record) {
        SbdsProvider.getInstance().getDatabase().queueSave(record);
    }

}
