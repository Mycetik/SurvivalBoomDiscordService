package net.survivalboom.sbds.api.service;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IServiceProvider {

    @NotNull IServiceProvider.RegisteredService registerService(@NotNull IModule module, @NotNull Object service);

    default @NotNull IServiceProvider.RegisteredService registerService(@NotNull ModuleMain main, @NotNull Object service) {
        return registerService(main.getModule(), service);
    }

    void unregisterService(@NotNull IModule module, @NotNull Class<?> clazz);

    default void unregisterService(@NotNull ModuleMain main, @NotNull Class<?> clazz) {
        unregisterService(main.getModule(), clazz);
    }


    @Nullable <T> T getService(@NotNull Class<T> clazz);

    @Nullable RegisteredService getRegisteredService(@NotNull Class<?> clazz);


    @NotNull List<RegisteredService> getRegisteredServices();


    record RegisteredService(@NotNull IModule module, @NotNull Class<?> clazz, @NotNull Object service) {}

}
