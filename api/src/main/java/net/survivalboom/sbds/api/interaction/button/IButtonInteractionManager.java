package net.survivalboom.sbds.api.interaction.button;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface IButtonInteractionManager {

    void registerPendingInteraction(@NotNull String id, @NotNull Consumer<ButtonInteractionInfo> onSuccess, @NotNull Runnable onFail, long timeout);

}
