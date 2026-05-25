package net.survivalboom.sbds.api.registrations;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RegistrationManager<T> extends Manager implements IRegistrationManager<T> {

    protected static final Logger log = LoggerFactory.getLogger(RegistrationManager.class);

    protected final IRegistrationRegistry registry;

    protected final String sourceName;

    @Nullable
    protected final Callback<T> callback;

    protected final Map<NamespacedKey, Registration<T>> registrationMap = new HashMap<>();


    public RegistrationManager(
            @NotNull String sourceName,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {

        Objects.requireNonNull(sourceName, "sourceName == null");
        Objects.requireNonNull(registry, "registry == null");

        this.sourceName = sourceName;
        this.callback = callback;
        this.registry = registry;

    }

    public RegistrationManager(
            @NotNull Manager manager,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        this(manager.getManagerName(), callback, registry);
    }

    public RegistrationManager(
            @NotNull Manager manager,
            @NotNull String subname,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        this(manager.getManagerName() + "." + subname, callback, registry);
    }

    public RegistrationManager(
            @NotNull Valid valid,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        this(valid.getClass().getSimpleName(), callback, registry);
    }

    public RegistrationManager(
            @NotNull Valid valid,
            @NotNull String subname,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {
        this(valid.getClass().getSimpleName() + "." + subname, callback, registry);
    }


    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {
        getRegistrations().forEach(this::unregister);
    }

    //
    // REGISTRATION
    //

    @Override
    public @NotNull Registration<T> register(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull T object
    ) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(object, "object == null");
        checkValid();

        var reg = registry.register(module, object, this::unreg0, sourceName, name);

        if (callback == null) {
            registrationMap.put(reg.regKey(), reg);
            return reg;
        }

        try {
            callback.onRegister(reg);
        }

        catch (Exception e) {
            log.error("An exception was thrown in register callback for registration `{}`.", reg, e);
        }

        registrationMap.put(reg.regKey(), reg);

        return reg;

    }

    protected void unreg0(@NotNull Registration<T> reg) {

        registrationMap.remove(reg.regKey());

        if (callback == null) {
            return;
        }

        try {
            callback.unRegister(reg);
        }

        catch (Exception e) {
            log.error("An exception was thrown in unregister callback for registration `{}`.", reg, e);
        }

    }

    @Override
    public boolean unregister(@NotNull Registration<T> registration) {

        Objects.requireNonNull(registration, "registration == null");
        checkValid();

        if (!registrationMap.containsKey(registration.regKey())) {
            return false;
        }

        registry.removeRegistration(registration);

        return true;

    }

    //
    // GETTERS
    //

    @Override
    public @NotNull List<Registration<T>> getRegistrations() {
        return new ArrayList<>(registrationMap.values());
    }

    @Override
    public @Nullable Registration<T> getRegistration(@NotNull NamespacedKey key) {
        return registrationMap.get(key);
    }

    //
    // MISC
    //

    @Override
    public String toString() {
        return String.format("%s{name=%s, registrations=%s}", this.getClass().getSimpleName(), sourceName, registrationMap.size());
    }


    //
    // CALLBACK
    //

    public interface Callback<T> {

        default void onRegister(@NotNull Registration<T> registration) {}

        default void unRegister(@NotNull Registration<T> registration) {}

    }

}
