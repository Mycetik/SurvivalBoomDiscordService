package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public interface CanModal<TModalCallback extends IModalCallback> extends InteractionHolder<TModalCallback> {

    default @NotNull ModalActionBuilder replyModal(@NotNull String key) {
        return new ModalActionBuilder(sbds().getModalInteractionManager(), interaction(), NamespacedKey.fromString(key));
    }

}
