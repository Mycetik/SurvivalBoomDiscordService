package net.survivalboom.sbds.api.utils.placeholders;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public interface IPlaceholderRegistry extends IManager {

    <V> @NotNull IRegisteredPlaceholderProvider<V> registerProvider(
            @NotNull IModule module,
            @NotNull Class<V> clazz,
            @NotNull Function<V, IPlaceholders> function
    );

    boolean unregisterProvider(@NotNull IRegisteredPlaceholderProvider<?> reg);


    @NotNull List<IRegisteredPlaceholderProvider<?>> getProviders();

    @Nullable IRegisteredPlaceholderProvider<?> getProvider(@NotNull NamespacedKey key);

    default @Nullable IRegisteredPlaceholderProvider<?> getProvider(@NotNull String key) {
        return getProvider(NamespacedKey.fromString(key));
    }

    <V> @Nullable IRegisteredPlaceholderProvider<V> getProviderFor(@NotNull V object);


    interface IRegisteredPlaceholderProvider<V> {

        @NotNull Class<V> getClazz();

        @NotNull Function<V, IPlaceholders> getFunction();

        @NotNull Registration<IRegisteredPlaceholderProvider<V>> getRegistration();

    }


}
