package net.survivalboom.sbds.api.interaction;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;

public abstract class ExecutionInfo {

    protected final ISBDS sbds;

    protected final Logger logger;


    public ExecutionInfo(@NotNull ISBDS sbds, @NotNull Logger logger) {
        this.sbds = sbds;
        this.logger = logger;
    }


    public @NotNull ISBDS sbds() {
        return sbds;
    }

    public @NotNull IMessages messages() {
        return sbds.getMessages();
    }

    public @NotNull Logger logger() {
        return logger;
    }

}
