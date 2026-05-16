package net.survivalboom.sbds.core.interaction.modal;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.interaction.modal.ModalInteractionInfo;
import net.survivalboom.sbds.api.interaction.modal.ModalTemplate;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public class ModalInteractionManager extends Manager implements IModalInteractionManager, EventListener {

    private static final Logger log = LoggerFactory.getLogger(ModalInteractionManager.class.getSimpleName());


    private final SBDS sbds;

    private final InternalRegistrationManager<IRegisteredModalTemplate> modalTemplateRegistry;

    private final InternalRegistrationManager<IRegisteredModal> staticModalRegistry;

    private final Map<String, PendingModal> pendingModalMap = new HashMap<>();


    private ISchedulerTask task;


    public ModalInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.modalTemplateRegistry = new InternalRegistrationManager<>(this, "template", null, sbds.getRegistrationRegistry());
        this.staticModalRegistry = new InternalRegistrationManager<>(this, "listener", null, sbds.getRegistrationRegistry());
    }

    //
    // MANAGER
    //

    @Override
    public @NotNull ISBDS getSbds() {
        return sbds;
    }

    @Override
    protected void init0() {

        modalTemplateRegistry.init();
        staticModalRegistry.init();

        sbds.getEventManager().registerEvents0(null, this);
        task = sbds.getScheduler().schedule0(null, "ModalInteractionManager-TimeoutChecker", task -> timeoutChecker(), 1000, 1000);

    }

    @Override
    protected void shutdown0() {

        sbds.getEventManager().unregisterEvents(this);

        task.cancelAndWait(5000, true);
        task = null;

        pendingModalMap.clear();

        staticModalRegistry.shutdown();
        modalTemplateRegistry.shutdown();

    }

    private void timeoutChecker() {

        var pendingModals = new HashMap<>(this.pendingModalMap);

        long time = System.currentTimeMillis();
        for (var entry : pendingModals.entrySet()) {

            String id = entry.getKey();
            IPendingModal modal = entry.getValue();

            if (modal.getTimestamp() + modal.getTimeout() > time) {
                continue;
            }

            this.pendingModalMap.remove(id);
            log.info("Pending modal `{}` expired.", id);

            Runnable failureCallback = modal.getFailureCallback();
            if (failureCallback == null) {
                continue;
            }

            try {
                failureCallback.run();
            }

            catch (Throwable t) {
                log.error("An exception was thrown while attempting to run failure callback for modal `{}`.", id, t);
            }

        }

    }

    @EventHandler
    public void onModal(ModalInteractionEvent event) {

        String id = event.getModalId();

        try {

            if (id.contains(":")) {
                processStatic(event);
            } else {
                processPending(event);
            }

        }

        catch (Throwable t) {

            log.error("An exception was thrown while tried to process modal `{}`.", id, t);

            sbds.getMessages().reply(event, "sbds.invalid-interaction", event.getMember())
                    .withPlaceholders("{exception}", t)
                    .send()
                    .setEphemeral(true)
                    .queue();

        }

    }

    private void processStatic(@NotNull ModalInteractionEvent event) {

        String id = event.getModalId();
        NamespacedKey key = NamespacedKey.fromString(id);

        User user = event.getUser();

        IRegisteredModal listener = staticModalRegistry.getRegistrationAsObject(key);

        if (listener == null) {

            sbds.getMessages().reply(event, "sbds.invalid-interaction", user)
                    .withPlaceholders("{id}", id)
                    .send()
                    .setEphemeral(true)
                    .queue();

            return;

        }

        ModalInteractionInfo info = new ModalInteractionInfo(sbds, event);
        listener.getExecutor().accept(info);

    }

    private void processPending(@NotNull ModalInteractionEvent event) {

        String id = event.getModalId();
        IPendingModal pending = pendingModalMap.get(id);

        User user = event.getUser();

        if (pending == null) {

            sbds.getMessages().reply(event, "sbds.invalid-interaction", user)
                    .withPlaceholders("{id}", id)
                    .send()
                    .setEphemeral(true)
                    .queue();

            return;
        }

        pendingModalMap.remove(id);

        ModalInteractionInfo info = new ModalInteractionInfo(sbds, event);
        pending.getSuccessCallback().accept(info);

    }

    //
    // MODAL TEMPLATES
    //

    // REG //

    @Override
    public @NotNull IRegisteredModalTemplate registerModalTemplate(@NotNull IModule module, @NotNull String name, @NotNull ModalTemplate modal) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(modal, "modal == null");
        checkValid();

        RegisteredModalTemplate modalTemplate = new RegisteredModalTemplate(this, modal);
        modalTemplate.registration = modalTemplateRegistry.register(module, name, modalTemplate);

        return modalTemplate;

    }

    // UNREG //

    @Override
    public boolean unregisterModalTemplate(@NotNull IRegisteredModalTemplate modal) {
        checkValid();
        return modalTemplateRegistry.unregister(modal) != null;
    }

    // GETTERS //

    @Override
    public @NotNull List<IRegisteredModalTemplate> getRegisteredModalTemplates() {
        checkValid();
        return modalTemplateRegistry.getRegisteredObjects();
    }

    @Override
    public @Nullable IRegisteredModalTemplate getRegisteredModalTemplate(@NotNull NamespacedKey key) {
        checkValid();
        return modalTemplateRegistry.getRegistrationAsObject(key);
    }

    //
    // PENDING MODALS
    //

    // REG //

    @Override
    public @NotNull IPendingModal registerPendingModal(
            @NotNull String id,
            @NotNull Consumer<ModalInteractionInfo> successCallback,
            @Nullable Runnable failureCallback,
            int timeout
    ) {

        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(successCallback, "successCallback == null");
        checkValid();

        if (pendingModalMap.containsKey(id)) {
            throw new IllegalStateException("Pending modal with id `" + id + "` already exists");
        }

        PendingModal pendingModal = new PendingModal(this, id, successCallback, failureCallback, timeout);
        pendingModalMap.put(id, pendingModal);

        return pendingModal;

    }

    // UNREG //

    @Override
    public @Nullable IPendingModal forgetPendingModal(@NotNull String id) {
        checkValid();
        return pendingModalMap.remove(id);
    }

    // GETTERS //

    @Override
    public @Nullable IPendingModal getPendingModal(@NotNull String id) {
        checkValid();
        return pendingModalMap.get(id);
    }

    @Override
    public @NotNull List<IPendingModal> getPendingModals() {
        checkValid();
        return new ArrayList<>(pendingModalMap.values());
    }

    //
    // MODAL LISTENERS
    //

    // REG //

    @Override
    public @NotNull IRegisteredModal registerModal(@NotNull IModule module, @NotNull String name, @NotNull Consumer<ModalInteractionInfo> executor) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(executor, "executor == null");
        checkValid();

        RegisteredModal modal = new RegisteredModal(this, executor);
        modal.registration = staticModalRegistry.register(module, name, modal);

        return modal;

    }

    // UNREG //

    @Override
    public boolean unregisterModal(@NotNull IRegisteredModal modal) {
        checkValid();
        return staticModalRegistry.unregister(modal) != null;
    }

    // GETTERS //

    @Override
    public @Nullable IRegisteredModal getRegisteredModal(@NotNull NamespacedKey key) {
        checkValid();
        return staticModalRegistry.getRegistrationAsObject(key);
    }

    @Override
    public @NotNull List<IRegisteredModal> getRegisteredModals() {
        checkValid();
        return staticModalRegistry.getRegisteredObjects();
    }


    //
    // DATA CLASSES
    //

    public static class RegisteredModalTemplate implements IRegisteredModalTemplate {

        private final ModalInteractionManager manager;

        private final ModalTemplate template;

        private Registration<IRegisteredModalTemplate> registration;


        public RegisteredModalTemplate(
                @NotNull ModalInteractionManager manager,
                @NotNull ModalTemplate template
        ) {
            this.manager = manager;
            this.template = template;
        }


        @Override
        public @NotNull Registration<IRegisteredModalTemplate> getRegistration() {
            return registration;
        }

        @Override
        public @NotNull ModalTemplate getTemplate() {
            return template;
        }

        @Override
        public @NotNull ModalActionBuilder createModal(@NotNull IModalCallback interaction) {
            return new ModalActionBuilder(manager, interaction, registration.key());
        }

        @Override
        public @NotNull IModalInteractionManager getManager() {
            return manager;
        }

    }

    public static class PendingModal implements IPendingModal {

        private final ModalInteractionManager manager;

        private final String id;

        private final Consumer<ModalInteractionInfo> successfulCallback;

        private final Runnable failureCallback;

        private final long timestamp;

        private final int timeout;


        public PendingModal(
                @NotNull ModalInteractionManager manager,
                @NotNull String id,
                @NotNull Consumer<ModalInteractionInfo> successfulCallback,
                @Nullable Runnable failureCallback,
                int timeout
        ) {

            this.manager = manager;
            this.id = id;

            this.successfulCallback = successfulCallback;
            this.failureCallback = failureCallback;

            this.timestamp = System.currentTimeMillis();
            this.timeout = timeout;

        }



        @Override
        public @NotNull String getId() {
            return id;
        }

        @Override
        public @NotNull Consumer<ModalInteractionInfo> getSuccessCallback() {
            return successfulCallback;
        }

        @Override
        public @Nullable Runnable getFailureCallback() {
            return failureCallback;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public int getTimeout() {
            return timeout;
        }

        @Override
        public @NotNull IModalInteractionManager getManager() {
            return manager;
        }

    }

    public static class RegisteredModal implements IRegisteredModal {

        private final ModalInteractionManager manager;

        private final Consumer<ModalInteractionInfo> executor;

        private Registration<IRegisteredModal> registration;


        public RegisteredModal(
                @NotNull ModalInteractionManager manager,
                @NotNull Consumer<ModalInteractionInfo> executor
        ) {
            this.manager = manager;
            this.executor = executor;
        }


        @Override
        public @NotNull Registration<IRegisteredModal> getRegistration() {
            return registration;
        }

        @Override
        public @NotNull Consumer<ModalInteractionInfo> getExecutor() {
            return executor;
        }

        @Override
        public @NotNull IModalInteractionManager getManager() {
            return manager;
        }

    }

}
