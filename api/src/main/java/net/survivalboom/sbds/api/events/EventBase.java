package net.survivalboom.sbds.api.events;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class EventBase {

    protected final ISBDS sbds;

    protected final IModule module;

    public EventBase(@NotNull ISBDS sbds) {
        Objects.requireNonNull(sbds, "sbds == null");
        this.sbds = sbds;
        this.module = null;
    }

    public EventBase(@NotNull IModule module) {
        Objects.requireNonNull(module, "module == null");
        this.sbds = module.getMain().getSbds();
        this.module = module;
    }

    public EventBase(@NotNull ModuleMain module) {
        Objects.requireNonNull(module, "module == null");
        this.sbds = module.getSbds();
        this.module = module.getModule();
    }

    public @NotNull ISBDS getSbds() {
        return sbds;
    }

    public IModule getModule() {
        return module;
    }

}
