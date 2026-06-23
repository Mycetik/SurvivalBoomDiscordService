package net.survivalboom.sbds.api.events;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;

public abstract class EventCancellableBase extends EventBase implements ICancellable {

    protected boolean cancelled = false;

    public EventCancellableBase(@NotNull ISBDS sbds) {
        super(sbds);
    }

    public EventCancellableBase(@NotNull IModule module) {
        super(module);
    }

    public EventCancellableBase(@NotNull ModuleMain module) {
        super(module);
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

}
