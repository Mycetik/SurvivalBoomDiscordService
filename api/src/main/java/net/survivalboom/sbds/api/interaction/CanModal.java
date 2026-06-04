package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface CanModal extends InteractionHolder {

    @ApiStatus.Internal
    @NotNull IModalCallback modalCallback0();

    default @NotNull ModalActionBuilder replyModal(@NotNull String key) {
        return new ModalActionBuilder(sbds().getModalInteractionManager(), modalCallback0(), NamespacedKey.fromString(key));
    }

}
