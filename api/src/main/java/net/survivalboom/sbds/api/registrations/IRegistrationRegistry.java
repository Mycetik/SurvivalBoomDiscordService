package net.survivalboom.sbds.api.registrations;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface IRegistrationRegistry extends IManager {

    //
    // REGISTRATION
    //

    <T> @NotNull Registration<T> register(@NotNull IModule module, @NotNull T object, @NotNull Consumer<Registration<T>> unregisterAction, @NotNull Collection<String> names);

    <T> @NotNull Registration<T> register(@NotNull IModule module, @NotNull T object, @NotNull Consumer<Registration<T>> unregisterAction, @NotNull String... names);

    //
    // UNREGISTER
    //

    <T> boolean removeRegistration(@NotNull Registration<T> registration);

    <T> boolean unregister(@NotNull Registration<T> registration);

    //
    // GETTERS
    //

    @Nullable Registration<?> getRegistration(@NotNull NamespacedKey key);

    default @Nullable Registration<?> getRegistration(@NotNull String key) {
        return getRegistration(NamespacedKey.fromString(key));
    }


    @NotNull List<Registration<?>> getRegistrations();

    default @NotNull List<Registration<?>> getModuleRegistrations(@Nullable IModule module) {
        return getRegistrations().stream()
                .filter(reg -> Objects.equals(reg.module(), module))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    default <T> @Nullable Registration<T> getObjectRegistration(@NotNull T object) {
        return (Registration<T>) getRegistrations().stream()
                .filter(reg -> reg.object().equals(object))
                .findAny()
                .orElse(null);
    }

}
