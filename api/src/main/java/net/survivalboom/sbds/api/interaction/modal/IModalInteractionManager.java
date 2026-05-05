package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
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

    // REG //

    @NotNull IRegisteredModalTemplate registerModalTemplate(@NotNull IModule module, @NotNull String name, @NotNull ModalTemplate modal);

    default @NotNull IRegisteredModalTemplate registerModalTemplate(@NotNull ModuleMain main, @NotNull String name, @NotNull ModalTemplate modalTemplate) {
        Objects.requireNonNull(main, "main == null");
        return registerModalTemplate(main.getModule(), name, modalTemplate);
    }

    // UNREG //

    boolean unregisterModalTemplate(@NotNull IRegisteredModalTemplate modal);

    // GETTERS //

    @NotNull List<IRegisteredModalTemplate> getRegisteredModalTemplates();

    @Nullable IRegisteredModalTemplate getRegisteredModalTemplate(@NotNull NamespacedKey key);

    //
    // PENDING MODALS
    //

    // REG //

    @NotNull IPendingModal registerPendingModal(
            @NotNull String id,
            @NotNull Consumer<ModalInteractionInfo> successCallback,
            @Nullable Runnable failureCallback,
            int timeout
    );

    // UNREG //

    @Nullable IPendingModal forgetPendingModal(@NotNull String id);

    // GETTERS //

    @Nullable IPendingModal getPendingModal(@NotNull String id);

    @NotNull List<IPendingModal> getPendingModals();

    //
    // STATIC MODALS
    //

    // REG //

    @NotNull IRegisteredModal registerModal(@NotNull IModule module, @NotNull String name, @NotNull Consumer<ModalInteractionInfo> executor);

    default @NotNull IRegisteredModal registerModal(@NotNull ModuleMain module, @NotNull String name, @NotNull Consumer<ModalInteractionInfo> executor) {
        return registerModal(module.getModule(), name, executor);
    }

    // UNREG //

    boolean unregisterModal(@NotNull IRegisteredModal modal);

    // GETTERS //

    @Nullable IRegisteredModal getRegisteredModal(@NotNull NamespacedKey key);

    @NotNull List<IRegisteredModal> getRegisteredModals();



    interface IRegisteredModalTemplate {

        @NotNull Registration<IRegisteredModalTemplate> getRegistration();

        @NotNull ModalTemplate getTemplate();

        @NotNull ModalActionBuilder createModal(@NotNull IModalCallback interaction);


        @NotNull IModalInteractionManager getManager();

    }

    interface IPendingModal {

        @NotNull String getId();

        @NotNull Consumer<ModalInteractionInfo> getSuccessCallback();

        @Nullable Runnable getFailureCallback();


        long getTimestamp();

        int getTimeout();


        @NotNull IModalInteractionManager getManager();

    }

    interface IRegisteredModal {

        @NotNull Registration<IRegisteredModal> getRegistration();

        @NotNull Consumer<ModalInteractionInfo> getExecutor();


        @NotNull IModalInteractionManager getManager();

    }

}
