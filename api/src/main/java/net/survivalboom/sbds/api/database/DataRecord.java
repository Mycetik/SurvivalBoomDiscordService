package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.utils.Valid;

public abstract class DataRecord extends Valid {

    public void invalid() {
        valid(false);
    }

}
