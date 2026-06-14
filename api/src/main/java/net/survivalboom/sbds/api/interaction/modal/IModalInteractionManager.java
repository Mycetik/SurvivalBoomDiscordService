package net.survivalboom.sbds.api.interaction.modal;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public interface IModalInteractionManager extends IManager {

    @NotNull ISBDS getSbds();

    //
    // MODALS REGISTRY
    //

    @NotNull IRegisteredModal registerModal(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull ModalTemplate template,
            @Nullable Consumer<ModalInteractionInfo> executor
    );

    @NotNull IRegisteredModal registerModal(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Consumer<ModalTemplate.Builder> builder,
            @Nullable Consumer<ModalInteractionInfo> executor
    );

    boolean unregisterModal(@NotNull IRegisteredModal modal);

    @NotNull List<IRegisteredModal> getRegisteredModals();

    @Nullable IRegisteredModal getRegisteredModal(@NotNull NamespacedKey key);

    default @Nullable IRegisteredModal getRegisteredModal(@NotNull String key) {
        return getRegisteredModal(NamespacedKey.fromString(key));
    }

    //
    // MODAL SENDING
    //

    @NotNull ModalActionBuilder replyModal(
            @NotNull InteractionHolder interaction,
            @NotNull ModalTemplate template
    );

    @NotNull ModalActionBuilder replyModal(
            @NotNull InteractionHolder interaction,
            @NotNull Consumer<ModalTemplate.Builder> builder
    );

    @NotNull ModalActionBuilder replyModal(
            @NotNull InteractionHolder interaction,
            @NotNull NamespacedKey key
    );

    default @NotNull ModalActionBuilder replyModal(
            @NotNull InteractionHolder interaction,
            @NotNull String key
    ) {
        return replyModal(interaction, NamespacedKey.fromString(key));
    }

    @NotNull IPendingModal createPending(@NotNull ModalActionBuilder builder);

    @NotNull List<IPendingModal> getPendingModals();

    //
    // RECORDS
    //

    interface IRegisteredModal {

        @NotNull Registration<IRegisteredModal> getRegistration();

        @NotNull IModalInteractionManager getManager();


        @NotNull ModalTemplate getTemplate();

        @Nullable Consumer<ModalInteractionInfo> getExecutor();

    }

    interface IPendingModal {

        @NotNull String getId();

        @Nullable IRegisteredModal getOriginModal();

        @NotNull ModalTemplate getTemplate();


        @Nullable Consumer<ModalInteractionInfo> getSuccessCallback();

        @Nullable Runnable getFailureCallback();

        default @Nullable Consumer<ModalInteractionInfo> getEffectiveSuccessCallback() {

            var callback = getSuccessCallback();
            if (callback == null) {
                callback = Objects.requireNonNull(getOriginModal(), "no origin modal! something went wrong?").getExecutor();
            }

            return callback;

        }


        long getTimestamp();

        int getTimeout();


        @NotNull IModalInteractionManager getManager();

    }

}
