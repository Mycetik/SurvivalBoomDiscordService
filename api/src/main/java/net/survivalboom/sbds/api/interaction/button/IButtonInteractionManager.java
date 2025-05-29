package net.survivalboom.sbds.api.interaction.button;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IButtonInteractionManager {

    void registerPendingInteraction(@NotNull String id, @NotNull Consumer<ButtonInteractionInfo> onSuccess, @NotNull Runnable onFail, long timeout);


    @NotNull IRegisteredButton registerButton(@NotNull IModule iModule, @NotNull String name, @NotNull Consumer<ButtonInteractionInfo> consumer);

    void unregisterButton(@NotNull IModule module, @NotNull String name);


    interface IRegisteredButton {

        @Nullable IModule module();

        @NotNull NamespacedKey key();

    }

}
