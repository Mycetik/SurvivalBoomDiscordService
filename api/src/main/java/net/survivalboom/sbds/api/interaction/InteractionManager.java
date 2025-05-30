package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface InteractionManager<T> {

    void registerPendingInteraction(@NotNull String id, @Nullable User user, @NotNull Consumer<T> onSuccess, @Nullable Runnable onFail, long timeout);

    @NotNull IRegisteredListener registerListener(@NotNull IModule iModule, @NotNull String name, @NotNull Consumer<T> consumer);

    default @NotNull IRegisteredListener registerListener(@NotNull ModuleMain main, @NotNull String name, @NotNull Consumer<T> consumer) {
        return registerListener(main.getModule(), name, consumer);
    }

    void unregisterListener(@NotNull IModule module, @NotNull String name);


    interface IRegisteredListener {

        @Nullable IModule module();

        @NotNull NamespacedKey key();

    }

}
