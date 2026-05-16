package net.survivalboom.sbds.api.events;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.ThrowingConsumer;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public interface IEventManager extends IManager {

    //
    // EVENT HANDLERS
    //

    // REGISTER //

    <T> @NotNull IRegisteredEventHandler registerEvent(
            @NotNull IModule module,
            @NotNull Class<T> clazz,
            @NotNull ThrowingConsumer<T> consumer,
            @NotNull EventPriority priority,
            boolean ignoreCancelled
    );

    @NotNull List<IRegisteredEventHandler> registerEvents(
            @NotNull IModule module,
            @NotNull EventListener listener
    );

    default <T> @NotNull IRegisteredEventHandler registerEvent(
            @NotNull ModuleMain module,
            @NotNull Class<T> clazz,
            @NotNull ThrowingConsumer<T> consumer,
            @NotNull EventPriority priority,
            boolean ignoreCancelled
    ) {
        return registerEvent(module.getModule(), clazz, consumer, priority, ignoreCancelled);
    }

    default @NotNull List<IRegisteredEventHandler> registerEvents(@NotNull ModuleMain main, @NotNull EventListener listener) {
        return registerEvents(main.getModule(), listener);
    }

    // UNREGISTER //

    boolean unregisterEvent(@NotNull IRegisteredEventHandler reg);

    @NotNull List<IRegisteredEventHandler> unregisterEvents(@NotNull EventListener listener);

    // GET //

    @NotNull List<IRegisteredEventHandler> getEventHandlers();


    interface IRegisteredEventHandler {

        @NotNull IEventManager getManager();

        @NotNull Class<?> getEventClass();

        @NotNull ThrowingConsumer<?> getEventHandler();

        @Nullable Class<? extends EventListener> getOriginClass();

        @NotNull EventPriority getPriority();

        boolean isIgnoringCancelled();

        @NotNull Registration<IRegisteredEventHandler> getRegistration();

    }

}
