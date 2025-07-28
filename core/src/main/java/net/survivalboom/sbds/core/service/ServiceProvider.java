package net.survivalboom.sbds.core.service;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.service.IServiceProvider;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.modules.ModuleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceProvider extends Manager implements IServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(ServiceProvider.class);
    private final ModuleManager moduleManager;

    private final Set<RegisteredService> registeredServices = new HashSet<>();


    public ServiceProvider(@NotNull SBDS sbds) {
        this.moduleManager = sbds.getModuleManager();
    }


    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {
        registeredServices.clear();
    }

    @Override
    public @NotNull IServiceProvider.RegisteredService registerService(@NotNull IModule module, @NotNull Object service) {

        checkValid();

        moduleManager.checkModuleEnabled(module, "Disabled module tried to register a service");

        Class<?> clazz = service.getClass();
        RegisteredService foundService = getRegisteredService(clazz);

        if (foundService!= null) {

            if (!foundService.module().equals(module)) {
                throw new IllegalStateException("Service `" + clazz.getSimpleName() + ".class` is already registered by module `" + module.getName() + " `. Access denied");
            }

            registeredServices.remove(foundService);

        }

        RegisteredService registeredService = new RegisteredService(module, clazz, service);
        registeredServices.add(registeredService);

        return registeredService;

    }

    @Override
    public void unregisterService(@NotNull IModule module, @NotNull Class<?> clazz) {

        checkValid();

        moduleManager.checkModuleEnabled(module, "Disabled module tried to unregister a service");
        RegisteredService service = getRegisteredService(clazz);
        if (service == null) {
            return;
        }

        if (!service.module().equals(module)) {
            throw new IllegalStateException("Service `" + service.getClass() + ".class` was registered by module `" + service.module().getName() + "`. Access denied");
        }

    }

    @Override
    public @Nullable RegisteredService getRegisteredService(@NotNull Class<?> clazz) {

        checkValid();

        List<RegisteredService> services = registeredServices.stream().filter(r -> r.getClass().isAssignableFrom(clazz)).toList();
        if (services.isEmpty()) {
            return null;
        }

        if (services.size() > 1) {
            throw new IllegalStateException("Multiple services found for requested class `" + clazz.getSimpleName() + ".class`. " + services);
        }

        return services.getFirst();

    }

    @Override
    public @NotNull List<RegisteredService> getRegisteredServices() {
        return new ArrayList<>(registeredServices);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getService(@NotNull Class<T> clazz) {

        RegisteredService service = getRegisteredService(clazz);
        if (service == null) {
            return null;
        }

        return (T) service.service();

    }

}
