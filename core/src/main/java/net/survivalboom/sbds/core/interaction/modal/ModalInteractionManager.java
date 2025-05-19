package net.survivalboom.sbds.core.interaction.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.modals.Modal;
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
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class ModalInteractionManager extends Manager implements Listener, IModalInteractionManager {

    private static final Logger log = LoggerFactory.getLogger(ModalInteractionManager.class.getSimpleName());

    private final SBDS sbds;

    private final Messages messages;

    private final EventManager eventManager;

    private final ModuleManager moduleManager;


    private final Map<NamespacedKey, RegisteredModal> registeredModalMap = new HashMap<>();


    private final Map<String, PendingModal> pendingModals = new ConcurrentHashMap<>();

    private SchedulerTask cleanerTask;


    public ModalInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.messages = sbds.getMessages();
        this.moduleManager = sbds.getModuleManager();
        this.eventManager = sbds.getEventManager();
    }

    @Override
    protected void init0() {

        cleanerTask = sbds.getScheduler().schedule0(null, "ModalInteractionManager-Cleaner", task -> cleanup(), 300000, 300000);
        eventManager.registerEvents0(null, this);

    }

    @Override
    protected void shutdown0() {
        pendingModals.clear();
        eventManager.unregisterEvents(this);
        cleanerTask.cancelAndWait(1000, true);
    }

    private void cleanup() {

        long currentTime = System.currentTimeMillis();

        Collection<PendingModal> modals = pendingModals.values();
        List<PendingModal> expired = modals.stream().filter(modal -> modal.timestamp() + 300000 < currentTime).toList();

        expired.forEach(modal -> {
            modal.future.completeExceptionally(new TimeoutException("Modal time out"));
            modals.remove(modal);
        });

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
    // OPEN MODAL
    //

    private @NotNull CompletableFuture<ModalInteractionInfo> replyModal(@NotNull IModalCallback interaction, @NotNull RegisteredModal registeredModal, @Nullable Placeholders placeholders) {

        UUID uuid = UUID.randomUUID();
        Modal modal = registeredModal.template().create(uuid, messages, placeholders);
        String uuidStr = uuid.toString();

        CompletableFuture<ModalInteractionInfo> future = new CompletableFuture<>();

        interaction.replyModal(modal).queue();

        pendingModals.put(uuidStr, new PendingModal(future, System.currentTimeMillis()));

        return future;

    }

    //
    // HANDLER
    //

    @EventHandler
    public void onModal(@NotNull ModalInteractionEvent event) {

        String customId = event.getModalId();
        ModalInteractionInfo info = new ModalInteractionInfo(sbds, event.getInteraction());

        if (!pendingModals.containsKey(customId)) {
            event.reply("Error: Modal with id `" + customId + "` not found.").queue();
            log.error("Modal with id `{}` not found. Possibly timeout.", customId);
            return;
        }

        CompletableFuture<ModalInteractionInfo> future = pendingModals.get(customId).future;

        future.complete(info);
        pendingModals.remove(customId);

    }

    private record PendingModal(@NotNull CompletableFuture<ModalInteractionInfo> future, long timestamp) {}

    public record RegisteredModal(@NotNull ModalInteractionManager manager, @Nullable IModule registrar, @NotNull NamespacedKey name, @NotNull ModalTemplate template) implements IRegisteredModal {

        @Override
        public @NotNull CompletableFuture<ModalInteractionInfo> open(@NotNull IModalCallback interaction, @Nullable Placeholders placeholders) {
            return manager.replyModal(interaction, this, placeholders);
        }

    }

}
