package net.survivalboom.sbds.core.interaction.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
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
import java.util.stream.Collectors;

public class ModalInteractionManager extends Manager implements IModalInteractionManager, EventListener {

    private static final Logger log = LoggerFactory.getLogger(ModalInteractionManager.class.getSimpleName());


    private final SBDS sbds;

    private final InternalRegistrationManager<IRegisteredModal> registry;

    private final Map<String, PendingModal> pendingModalMap = new HashMap<>();


    private ISchedulerTask task;


    public ModalInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.registry = new InternalRegistrationManager<>(this, null, sbds.getRegistrationRegistry());
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

        registry.init();

        sbds.getEventManager().registerEvents0(null, this);
        task = sbds.getScheduler().schedule0(null, "ModalInteractionManager-TimeoutChecker", this::timeoutChecker, 1000, 1000);

    }

    @Override
    protected void shutdown0() {

        sbds.getEventManager().unregisterEvents(this);

        task.tryCancel();
        task = null;

        pendingModalMap.clear();

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

            String nameStr = modal.getOriginModal() != null ? modal.getOriginModal().getRegistration().key().toString() : id;

            this.pendingModalMap.remove(id);
            log.info("Pending modal `{}` expired.", nameStr);

            Runnable failureCallback = modal.getFailureCallback();
            if (failureCallback == null) {
                continue;
            }

            try {
                failureCallback.run();
            }

            catch (Throwable t) {
                log.error("An exception was thrown while attempting to run failure callback for modal `{}`.", nameStr, t);
            }

        }

    }

    @EventHandler
    public void onModal(ModalInteractionEvent event) {

        String id = event.getModalId();
        IPendingModal pendingModal = pendingModalMap.get(id);
        if (pendingModal == null) {
            sbds.getMessages().reply(event, "sbds.invalid-interaction", event.getUser())
                    .withPlaceholders("id", id)
                    .queue();
            return;
        }

        String nameStr = pendingModal.getOriginModal() != null ? pendingModal.getOriginModal().getRegistration().key().toString() : id;

        try {

            ModalInteractionInfo info = new ModalInteractionInfo(sbds, event);

            Consumer<ModalInteractionInfo> callback = pendingModal.getEffectiveSuccessCallback();
            if (callback == null) {
                sbds.getMessages().reply(event, "sbds.interaction-no-response", event.getUser()).queue();
                log.info("Modal interaction `{}` has no executor!", nameStr);
                return;
            }

            log.info("User &b{} &rsubmitted modal &b{} &rwith &e{}", event.getUser().getEffectiveName(), nameStr, info.fields().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getAsString())));

            pendingModalMap.remove(id);

            callback.accept(info);

        }

        catch (Throwable t) {
            log.error("An exception was thrown while tried to process modal `{}`.", nameStr, t);
            sbds.getMessages().reply(event, "sbds.error", event.getUser())
                    .withPlaceholders("exception", t)
                    .setEphemeral(true)
                    .queue();
        }

    }

    //
    // MODALS REGISTRY
    //

    // REG //

    public @NotNull IRegisteredModal registerModal0(
            @Nullable IModule module,
            @NotNull String name,
            @NotNull ModalTemplate template,
            @Nullable Consumer<ModalInteractionInfo> executor
    ) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(template, "template == null");
        checkValid();

        RegisteredModal modal = new RegisteredModal(this, template, executor);
        modal.registration = registry.register0(module, name, modal);

        return modal;

    }

    @Override
    public @NotNull IRegisteredModal registerModal(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull ModalTemplate template,
            @Nullable Consumer<ModalInteractionInfo> executor
    ) {
        Objects.requireNonNull(module, "module == null");
        return registerModal0(module, name, template, executor);
    }

    @Override
    public @NotNull IRegisteredModal registerModal(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Consumer<ModalTemplate.Builder> builder,
            @Nullable Consumer<ModalInteractionInfo> executor
    ) {

        ModalTemplate.Builder b = ModalTemplate.builder();
        builder.accept(b);

        ModalTemplate template = b.build();

        return registerModal(module, name, template, executor);

    }

    // UNREG //

    @Override
    public boolean unregisterModal(@NotNull IRegisteredModal modal) {
        checkValid();
        return registry.unregister(modal) != null;
    }

    // GETTERS //

    @Override
    public @Nullable IRegisteredModal getRegisteredModal(@NotNull NamespacedKey key) {
        checkValid();
        return registry.getRegistrationAsObject(key);
    }

    @Override
    public @NotNull List<IRegisteredModal> getRegisteredModals() {
        checkValid();
        return registry.getRegisteredObjects();
    }

    //
    // MODAL SENDING
    //

    @Override
    public @NotNull ModalActionBuilder replyModal(
            @NotNull InteractionHolder interaction,
            @NotNull ModalTemplate template
    ) {
        checkValid();
        return new ModalActionBuilder(interaction, template, this);
    }

    @Override
    public @NotNull ModalActionBuilder replyModal(
            @NotNull InteractionHolder interaction,
            @NotNull Consumer<ModalTemplate.Builder> builder
    ) {

        ModalTemplate.Builder b = ModalTemplate.builder();
        builder.accept(b);

        ModalTemplate template = b.build();

        return replyModal(interaction, template);

    }

    @Override
    public @NotNull ModalActionBuilder replyModal(
            @NotNull InteractionHolder interaction,
            @NotNull NamespacedKey key
    ) {

        IRegisteredModal modal = getRegisteredModal(key);
        if (modal == null) {
            throw new IllegalArgumentException("No modal with name `" + key + "` exists");
        }

        return new ModalActionBuilder(interaction, modal, this);

    }

    @Override
    public @NotNull IPendingModal createPending(@NotNull ModalActionBuilder builder) {

        checkValid();

        Consumer<ModalInteractionInfo> onSuccess = builder.getOnSuccess();
        Runnable onFail = builder.onFail();
        int timeout = builder.getTimeout();

        IRegisteredModal origin = builder.getOriginModal();
        if (origin == null) {
            Objects.requireNonNull(onSuccess, "onSuccess == null");
        }

        String id = UUID.randomUUID().toString();

        PendingModal pendingModal = new PendingModal(this, origin, builder.getTemplate(), id, onSuccess, onFail, timeout);
        pendingModalMap.put(id, pendingModal);

        return pendingModal;

    }

    @Override
    public @NotNull List<IPendingModal> getPendingModals() {
        return new ArrayList<>(pendingModalMap.values());
    }

    //
    // DATA CLASSES
    //

    public static class PendingModal implements IPendingModal {

        private final ModalInteractionManager manager;

        private final IRegisteredModal originModal;

        private final ModalTemplate template;

        private final String id;

        private final Consumer<ModalInteractionInfo> successfulCallback;

        private final Runnable failureCallback;

        private final long timestamp;

        private final int timeout;


        public PendingModal(
                @NotNull ModalInteractionManager manager,
                @Nullable IRegisteredModal originModal,
                @NotNull ModalTemplate template,
                @NotNull String id,
                @Nullable Consumer<ModalInteractionInfo> successfulCallback,
                @Nullable Runnable failureCallback,
                int timeout
        ) {

            this.manager = manager;
            this.originModal = originModal;
            this.template = template;
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
        public @Nullable IRegisteredModal getOriginModal() {
            return originModal;
        }

        @Override
        public @NotNull ModalTemplate getTemplate() {
            return template;
        }

        @Override
        public @Nullable Consumer<ModalInteractionInfo> getSuccessCallback() {
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

        private final ModalTemplate template;

        private final Consumer<ModalInteractionInfo> executor;

        private Registration<IRegisteredModal> registration;


        public RegisteredModal(
                @NotNull ModalInteractionManager manager,
                @NotNull ModalTemplate template,
                @Nullable Consumer<ModalInteractionInfo> executor
        ) {
            this.manager = manager;
            this.template = template;
            this.executor = executor;
        }


        @Override
        public @NotNull Registration<IRegisteredModal> getRegistration() {
            return registration;
        }

        @Override
        public @Nullable Consumer<ModalInteractionInfo> getExecutor() {
            return executor;
        }

        @Override
        public @NotNull IModalInteractionManager getManager() {
            return manager;
        }

        @Override
        public @NotNull ModalTemplate getTemplate() {
            return template;
        }

    }

}
