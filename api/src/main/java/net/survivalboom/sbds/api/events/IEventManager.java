package net.survivalboom.sbds.api.events;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;

public interface IEventManager {

    void registerEvents(@NotNull IModule module, @NotNull Listener listener);

    default void registerEvents(@NotNull ModuleMain moduleMain, @NotNull Listener listener) {
        registerEvents(moduleMain.getModule(), listener);
    }

    void unregisterEvents(@NotNull IModule module);

    void unregisterEvents(@NotNull Listener listener);

}
