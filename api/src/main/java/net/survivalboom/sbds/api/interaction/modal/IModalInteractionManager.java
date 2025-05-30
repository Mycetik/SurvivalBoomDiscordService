package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.survivalboom.sbds.api.interaction.InteractionManager;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public interface IModalInteractionManager extends InteractionManager<ModalInteractionInfo> {

    @NotNull IRegisteredModal registerModal(@NotNull IModule iModule, @NotNull String name, @NotNull ModalTemplate modal);

    default @NotNull IRegisteredModal registerModal(@NotNull ModuleMain main, @NotNull String name, @NotNull ModalTemplate modalTemplate) {
        Objects.requireNonNull(main, "main == null");
        return registerModal(main.getModule(), name, modalTemplate);
    }


    void unregisterModal(@NotNull IModule iModule, @NotNull String name);

    void unregisterModal(@NotNull IModule iModule, @NotNull IRegisteredModal modal);


    @NotNull ModalActionBuilder createModal(@NotNull IModalCallback callback, @NotNull String name);


    @Nullable IRegisteredModal getModal(@NotNull NamespacedKey key);

    @NotNull IMessages getMessages();


    interface IRegisteredModal {

        @NotNull ModalActionBuilder createModal(@NotNull IModalCallback interaction);

        @Nullable IModule registrar();

        @NotNull NamespacedKey name();

        @NotNull ModalTemplate template();

    }

}
