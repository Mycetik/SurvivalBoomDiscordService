package net.survivalboom.sbds.core.interaction.context;

import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.interaction.context.IContextInteractionManager;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.core.SBDS;
import org.jetbrains.annotations.NotNull;

public class ContextInteractionManager extends Manager implements Listener, IContextInteractionManager {

    private final SBDS sbds;

    public ContextInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
    }

    @Override
    protected void init0() {

        sbds.getEventManager().registerEvents0(null, this);

    }

    @Override
    protected void shutdown0() {
        sbds.getEventManager().unregisterEvents(this);
    }


//    @EventHandler
//    public void onContext(Contex) {
//
//
//
//    }

}
