package net.survivalboom.sbds.core.interaction.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import net.survivalboom.sbds.api.interaction.modal.ModalInteractionInfo;
import net.survivalboom.sbds.api.interaction.modal.ModalTemplate;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.events.EventManager;
import net.survivalboom.sbds.core.messages.Messages;
import net.survivalboom.sbds.core.modules.Module;
import net.survivalboom.sbds.core.modules.ModuleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class ModalInteractionManager extends Manager implements Listener, IModalInteractionManager {

    private static final Logger log = LoggerFactory.getLogger(ModalInteractionManager.class.getSimpleName());

    private final SBDS sbds;

    private final Messages messages;

    private final EventManager eventManager;

    private final ModuleManager moduleManager;

    private final Map<String, CompletableFuture<ModalInteractionInfo>> pendingModals = new ConcurrentHashMap<>();

    private final Map<NamespacedKey, RegisteredModal> registeredModalMap = new HashMap<>();


    public ModalInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.messages = sbds.getMessages();
        this.moduleManager = sbds.getModuleManager();
        this.eventManager = sbds.getEventManager();
    }

    @Override
    protected void init0() {
        eventManager.registerEvents0(null, this);
    }

    @Override
    protected void shutdown0() {
        pendingModals.clear();
        eventManager.unregisterEvents(this);
    }

    //
    // REGISTERING
    //

    @Override
    public @NotNull IRegisteredModal registerModal(@NotNull IModule iModule, @NotNull String name, @NotNull ModalTemplate modal) {

        Objects.requireNonNull(iModule, "module == null");

        return registerModal0(iModule, name, modal);

    }

    public @NotNull RegisteredModal registerModal0(@NotNull IModule imodule, @NotNull String name, @NotNull ModalTemplate modal) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(modal, "modal == null");

        Module module = moduleManager.checkModuleEnabled(imodule, "Disabled module `" + imodule.getName() + "` tried to register a modal");

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

        moduleManager.checkModuleEnabled(iModule, "Disabled module `" + iModule.getName() + "` tried unregister a modal");

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
    public @Nullable IRegisteredModal getModal(@NotNull NamespacedKey key) {
        return registeredModalMap.get(key);
    }

    //
    // HANDLER
    //

    @EventHandler
    public void onModal(@NotNull ModalInteractionEvent event) {

        String customId = event.getModalId();
        ModalInteractionInfo info = new ModalInteractionInfo(sbds, event.getInteraction());

        CompletableFuture<ModalInteractionInfo> future = pendingModals.get(customId);
        if (future == null) {
            event.reply("Error: Modal with id `" + customId + "` not found.").queue();
            log.error("Modal with id `{}` not found. Possibly timeout.", customId);
            return;
        }

        future.complete(info);
        pendingModals.remove(customId);

    }


    public record RegisteredModal(@NotNull ModalInteractionManager manager, @Nullable IModule registrar, @NotNull NamespacedKey name, @NotNull ModalTemplate template) implements IRegisteredModal {

        @Override
        public @NotNull CompletableFuture<ModalInteractionInfo> open(@NotNull IModalCallback interaction, @Nullable Placeholders placeholders) {

            UUID uuid = UUID.randomUUID();
            Modal modal = template.create(uuid, manager.messages, placeholders);

            String uuidStr = uuid.toString();

            CompletableFuture<ModalInteractionInfo> future = new CompletableFuture<>();

            manager.sbds.getScheduler().schedule0(null, null, task -> {
                if (future.isDone()) return;
                future.completeExceptionally(new TimeoutException("Modal timeout"));
                manager.pendingModals.remove(uuidStr);
//            }, 300000, 0);
            }, 10000, 0);

            manager.pendingModals.put(uuidStr, future);
            interaction.replyModal(modal).complete();

            return future;

        }

    }

}
