package net.survivalboom.sbds.core.registration;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.IRegistrationRegistry;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.registrations.RegistrationManager;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class InternalRegistrationManager<T> extends RegistrationManager<T> {

    public InternalRegistrationManager(
            @NotNull String sourceName,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        super(sourceName, callback, registry);
    }

    public InternalRegistrationManager(
            @NotNull Manager manager,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        super(manager, callback, registry);
    }

    public InternalRegistrationManager(
            @NotNull Manager manager,
            @NotNull String subname,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        super(manager, subname, callback, registry);
    }

    public InternalRegistrationManager(
            @NotNull Valid valid,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        super(valid, callback, registry);
    }

    public InternalRegistrationManager(
            @NotNull Valid valid,
            @NotNull String subname,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        super(valid, subname, callback, registry);
    }

    public @NotNull Registration<T> register0(
            @Nullable IModule module,
            @NotNull String name,
            @NotNull T object
    ) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(object, "object == null");
        checkValid();

        RegistrationRegistry registry = (RegistrationRegistry) this.registry;
        var reg = registry.register0(module, object, this::unreg0, sourceName, name);

        if (callback != null) {
            callback.onRegister(reg);
        }

        registrationMap.put(reg.key(), reg);

        return reg;

    }

}
