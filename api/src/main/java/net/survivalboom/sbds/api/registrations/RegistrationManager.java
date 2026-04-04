package net.survivalboom.sbds.api.registrations;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Manager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RegistrationManager<T> extends Manager implements IRegistrationManager<T> {

    private static final Logger log = LoggerFactory.getLogger(RegistrationManager.class);

    private final IRegistrationRegistry registry;

    @Nullable
    private final String sourceName;

    @Nullable
    private final Callback<T> callback;

    private final Map<NamespacedKey, Registration<T>> registrationMap = new HashMap<>();


    public RegistrationManager(
            @Nullable String sourceName,
            @Nullable Callback<T> callback,
            @NotNull IRegistrationRegistry registry
    ) {

        Objects.requireNonNull(registry, "registry == null");

        this.sourceName = sourceName;
        this.callback = callback;
        this.registry = registry;

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

        List<String> names = new ArrayList<>();
        if (this.sourceName != null) {
            names.add(sourceName);
        }
        names.add(name);

        var reg = registry.register(module, object, this::unreg0, names);

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

    private void unreg0(@NotNull Registration<T> reg) {

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

        registry.unregister(registration);

        return true;

    }

    //
    // GETTERS
    //

    @Override
    public @NotNull List<Registration<T>> getRegistrations() {
        return List.of();
    }

    @Override
    public @Nullable Registration<T> getRegistration(@NotNull NamespacedKey key) {
        return registrationMap.get(key);
    }

    //
    // CALLBACK
    //

    public interface Callback<T> {

        void onRegister(@NotNull Registration<T> registration);

        void unRegister(@NotNull Registration<T> registration);

    }

}
