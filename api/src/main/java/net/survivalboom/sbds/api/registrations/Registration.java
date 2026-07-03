package net.survivalboom.sbds.api.registrations;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record Registration<object>(
        @NotNull String name,
        @NotNull NamespacedKey key,
        @Nullable IModule module,
        @NotNull NamespacedKey regKey,
        @NotNull object object,
        @NotNull Consumer<Registration<object>> unregisterAction
) {}
