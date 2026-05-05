package net.survivalboom.sbds.api.interaction;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;

public abstract class ExecutionInfo {

    protected final ISBDS sbds;


    public ExecutionInfo(
            @NotNull ISBDS sbds
    ) {
        this.sbds = sbds;
    }


    public @NotNull ISBDS sbds() {
        return sbds;
    }

    public @NotNull IMessages messages() {
        return sbds.getMessages();
    }

}
