package net.survivalboom.sbds.core.interaction.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.interaction.modal.ModalInteractionInfo;
import net.survivalboom.sbds.api.interaction.modal.ModalTemplate;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.interaction.AbstractInteractionHandler;
import net.survivalboom.sbds.core.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModalInteractionManager extends AbstractInteractionHandler<ModalInteractionInfo, ModalInteractionEvent> implements IModalInteractionManager {

    private final Map<NamespacedKey, RegisteredModal> registeredModalMap = new HashMap<>();

    public ModalInteractionManager(@NotNull SBDS sbds) {
        super("Modal", sbds);
    }

    @Override
    protected @Nullable String getIdFromEvent(ModalInteractionEvent event) {
        return event.getModalId();
    }

    @Override
    protected @NotNull ModalInteractionInfo createInteractionInfo(ModalInteractionEvent event) {
        return new ModalInteractionInfo(sbds, logger, event);
    }

    @Override
    public @NotNull IRegisteredModal registerModal(@NotNull IModule iModule, @NotNull String name, @NotNull ModalTemplate modal) {

        Objects.requireNonNull(iModule, "module == null");

        return registerModal0(iModule, name, modal);

    }

    public @NotNull RegisteredModal registerModal0(@NotNull IModule imodule, @NotNull String name, @NotNull ModalTemplate modal) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(modal, "modal == null");

        Module module = sbds.getModuleManager().checkModuleEnabled(imodule, "Disabled module `" + imodule.getName() + "` tried to register a modal");

        NamespacedKey key = NamespacedKey.fromModule(imodule, name);
        if (registeredModalMap.containsKey(key)) throw new IllegalArgumentException("Modal with key `" + key + "` already exists");

        RegisteredModal registeredModal = new RegisteredModal(this, imodule, key, modal);
        registeredModalMap.put(key, registeredModal);

        module.getRegistration().add("Modal-" + name, () -> unregisterModal(imodule, registeredModal));

        return registeredModal;

    }


    @Override
    public void unregisterModal(@NotNull IModule iModule, @NotNull String name) {

        Objects.requireNonNull(name, "name == null");

        sbds.getModuleManager().checkModuleEnabled(iModule, "Disabled module `" + iModule.getName() + "` tried unregister a modal");

        NamespacedKey key = NamespacedKey.fromModule(iModule, name);
        registeredModalMap.remove(key);

    }

    @Override
    public void unregisterModal(@NotNull IModule iModule, @NotNull IRegisteredModal modal) {

        Objects.requireNonNull(modal, "modal == null");

//        moduleManager.checkModuleEnabled(iModule, "Disabled module `" + iModule.getName() + "` tried unregister a modal");

        registeredModalMap.remove(modal.name());

    }

    @Override
    public @NotNull ModalActionBuilder createModal(@NotNull IModalCallback callback, @NotNull String name) {
        NamespacedKey key = NamespacedKey.fromString(name);
        return new ModalActionBuilder(this, callback, key);
    }

    @Override
    public @Nullable RegisteredModal getModal(@NotNull NamespacedKey key) {
        return registeredModalMap.get(key);
    }

    @Override
    public @NotNull IMessages getMessages() {
        return sbds.getMessages();
    }

    //
    // HANDLER
    //

    @EventHandler
    public void onModal(@NotNull ModalInteractionEvent event) {
        onEvent(event);
    }

    // TODO Зробити так, щоби кожен раз при відкритті Modal, ми не шукали його ще раз по назві.
    public record RegisteredModal(
            @NotNull ModalInteractionManager manager,
            @Nullable IModule registrar,
            @NotNull NamespacedKey name,
            @NotNull ModalTemplate template
    ) implements IRegisteredModal {

        @Override
        public @NotNull ModalActionBuilder createModal(@NotNull IModalCallback callback) {
            return manager.createModal(callback, name.toString());
        }

    }

}
