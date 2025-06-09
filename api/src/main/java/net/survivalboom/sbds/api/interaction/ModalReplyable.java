package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public interface ModalReplyable {

    @NotNull ISBDS sbds();

    @NotNull IModalCallback modalCallback();

    @NotNull User user();

    @NotNull IMessages messages();

    default @NotNull ModalActionBuilder replyModal(@NotNull String key) {
        return new ModalActionBuilder(sbds().getModalInteractionManager(), modalCallback(), NamespacedKey.fromString(key));
    }

}
