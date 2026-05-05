package net.survivalboom.sbds.api.registrations;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface IRegistrationManager<T> {

    @NotNull Registration<T> register(@NotNull IModule module, @NotNull String name, @NotNull T object);

    boolean unregister(@NotNull Registration<T> registration);

    default @Nullable Registration<T> unregister(@NotNull T obj) {

        var reg = getObjectRegistration(obj);
        if (reg == null) {
            return null;
        }

        unregister(reg);

        return reg;

    }

    //
    // GETTERS
    //

    @NotNull List<Registration<T>> getRegistrations();

    default @NotNull List<T> getRegisteredObjects() {
        return getRegistrations().stream().map(Registration::object).collect(Collectors.toList());
    }

    @Nullable Registration<T> getRegistration(@NotNull NamespacedKey key);

    default @Nullable T getRegistrationAsObject(@NotNull NamespacedKey key) {

        var reg = getRegistration(key);
        if (reg == null) {
            return null;
        }

        return reg.object();

    }

    default @Nullable Registration<T> getRegistration(@NotNull String name) {
        return getRegistration(NamespacedKey.fromString(name));
    }

    default @Nullable Registration<T> getObjectRegistration(@NotNull T object) {

        return getRegistrations().stream()
                .filter(reg -> Objects.equals(reg.object(), object))
                .findAny()
                .orElse(null);

    }

}
