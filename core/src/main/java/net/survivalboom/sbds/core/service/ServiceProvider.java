package net.survivalboom.sbds.core.service;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.service.IServiceProvider;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ServiceProvider extends Manager implements IServiceProvider {

    private final ISBDS sbds;

    private final InternalRegistrationManager<IRegisteredService<?>> registry;


    public ServiceProvider(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.registry = new InternalRegistrationManager<>(this, null, sbds.getRegistrationRegistry());
    }


    @Override
    protected void init0() {
        registry.init();
    }

    @Override
    protected void shutdown0() {
        registry.shutdown();
    }

    // REG //

    @Override
    public <T> IServiceProvider.@NotNull IRegisteredService<T> registerService(@NotNull IModule module, @NotNull Class<T> clazz, @NotNull T service) {
        Objects.requireNonNull(module, "module == null");
        return registeredService0(module, clazz, service);
    }

    @SuppressWarnings("unchecked") // <-- Іді нахуй сука блять.
    public <T> IServiceProvider.@NotNull IRegisteredService<T> registeredService0(@Nullable IModule module, @NotNull Class<T> clazz, @NotNull T service) {

        Objects.requireNonNull(service, "service == null");
        Objects.requireNonNull(clazz, "clazz == null");
        checkValid();

        if (module != null) {
            sbds.getModuleManager().checkModuleEnabled(module, "Disabled module tried to register a service");
        }

        IRegisteredService<?> foundService = getRegisteredService(clazz);
        if (foundService != null) {
            throw new IllegalStateException("Service `" + clazz.getSimpleName() + ".class` is already registered `" + foundService.getRegistration().key() + " `. Access denied");
        }

        RegisteredService<T> registeredService = new RegisteredService<>(clazz, service, this);
        registeredService.registration = (Registration<IRegisteredService<T>>) (Registration<?>) registry.register0(module, clazz.getSimpleName(), registeredService);

        return registeredService;

    }

    // UNREG //

    @Override
    public boolean unregisterService(@NotNull IRegisteredService<?> reg) {
        checkValid();
        return registry.unregister(reg) != null;
    }

    // GET //

    @Override
    public @Nullable IRegisteredService<?> getRegisteredService(@NotNull NamespacedKey key) {
        checkValid();
        return registry.getRegistrationAsObject(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable IRegisteredService<T> getRegisteredService(@NotNull Class<T> clazz) {

        Objects.requireNonNull(clazz, "clazz == null");
        checkValid();

        List<IRegisteredService<?>> services = registry.getRegisteredObjects()
                .stream()
                .filter(r -> clazz.isAssignableFrom(r.getClazz()))
                .toList();

        if (services.isEmpty()) {
            return null;
        }

        if (services.size() > 1) {
            throw new IllegalStateException("Multiple services found for requested class `" + clazz.getSimpleName() + ".class`. " + services);
        }

        return (IRegisteredService<T>) services.getFirst();

    }

    @Override
    public @NotNull List<IRegisteredService<?>> getRegistry() {
        checkValid();
        return registry.getRegisteredObjects();
    }

    @Override
    public <T> @Nullable T getService(@NotNull Class<T> clazz) {

        var service = getRegisteredService(clazz);
        if (service == null) {
            return null;
        }

        return service.getObject();

    }

    //
    // RECORD
    //

    public static class RegisteredService<T> implements IRegisteredService<T> {

        private final Class<T> clazz;

        private final T object;

        private final ServiceProvider manager;

        private Registration<IRegisteredService<T>> registration;


        public RegisteredService(@NotNull Class<T> clazz, @NotNull T object, @NotNull ServiceProvider manager) {
            this.clazz = clazz;
            this.object = object;
            this.manager = manager;
        }

        @Override
        public @NotNull IServiceProvider getManager() {
            return manager;
        }

        @Override
        public @NotNull Registration<IRegisteredService<T>> getRegistration() {
            return registration;
        }

        @Override
        public @NotNull Class<T> getClazz() {
            return clazz;
        }

        @Override
        public @NotNull T getObject() {
            return object;
        }

    }

}
