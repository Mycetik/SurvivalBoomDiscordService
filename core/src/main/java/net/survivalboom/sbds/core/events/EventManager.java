package net.survivalboom.sbds.core.events;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.survivalboom.sbds.api.events.IEventManager;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.modules.ModuleManager;
import net.survivalboom.sbds.api.utils.Manager;
import org.jetbrains.annotations.NotNull;
import net.survivalboom.sbds.core.modules.Module;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EventManager extends Manager implements EventListener, IEventManager {

    private final Set<EventHandler> handlerList = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger("EventManager");

    private final SBDS sbds;

    public EventManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
    }

    @Override
    protected void init0() {
        sbds.getBot().addEventListener(this);
    }

    @Override
    protected void shutdown0() {
        handlerList.clear();
        sbds.getBot().removeEventListener(this);
    }


    @Override
    public void registerEvents(@NotNull IModule imodule, @NotNull Listener listener) {
        Objects.requireNonNull(imodule, "module == null");
        registerEvents0(imodule, listener);
    }

    public void registerEvents0(@Nullable IModule imodule, @NotNull Listener listener) {

        Objects.requireNonNull(listener, "listener == null");

        EventHandler eventHandler;

        if (imodule != null) {
            Module module = sbds.getModuleManager().checkModuleEnabled(imodule, "Disabled module attempted to register an event");
            eventHandler = new EventHandler(logger, module, listener);
            module.getRegistration().add("EventListener", () -> unregisterEvents(listener));
        }

        else eventHandler = new EventHandler(logger, null, listener);

        eventHandler.scan();

        handlerList.add(eventHandler);

    }

    @Override
    public void unregisterEvents(@NotNull Listener listener) {
        handlerList.removeIf(h -> h.getListener().equals(listener));
    }

    @Override
    public void unregisterEvents(@NotNull IModule module) {
        Objects.requireNonNull(module, "module == null");
        handlerList.removeIf(h -> module.equals(h.getModule()));
    }


    @Override
    public void onEvent(@NotNull GenericEvent event) {

        for (EventHandler handler : new ArrayList<>(handlerList)) {
            handler.onEvent(event);
        }

    }

}
