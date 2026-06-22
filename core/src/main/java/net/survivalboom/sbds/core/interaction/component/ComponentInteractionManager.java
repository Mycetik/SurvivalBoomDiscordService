package net.survivalboom.sbds.core.interaction.component;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.api.interaction.component.ComponentInteractionInfo;
import net.survivalboom.sbds.api.interaction.component.ComponentInteractionRequest;
import net.survivalboom.sbds.api.interaction.component.IComponentInteractionManager;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.permissions.Permission;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.api.utils.valid.Valid;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ComponentInteractionManager extends Manager implements IComponentInteractionManager, EventListener {

    private static final Logger log = LoggerFactory.getLogger(ComponentInteractionManager.class.getSimpleName());


    private final SBDS sbds;

    private final InternalRegistrationManager<IRegisteredListener<?>> registry;

    private final Set<IPendingInteraction> pendingInteractions = new HashSet<>();

    private ISchedulerTask task;


    public ComponentInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.registry = new InternalRegistrationManager<>(this, null, sbds.getRegistrationRegistry());
    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {

        registry.init();

        sbds.getEventManager().registerEvents0(null, this);
        task = sbds.getScheduler().schedule0(null, "ComponentInteractionManager-TimeoutChecker", task -> timeoutChecker(), 1000, 1000);

    }

    @Override
    protected void shutdown0() {

        task.tryCancel();
        task = null;

        pendingInteractions.clear();
        registry.shutdown();

    }

    @EventHandler
    public void onComponentInteraction(@NotNull GenericComponentInteractionCreateEvent event) {

        String id = event.getComponentId();

        try {

            if (id.contains(":")) {
                processStatic(event);
            } else {
                processPending(event);
            }

        }

        catch (Throwable t) {

            log.error("An exception was thrown while tried to process interaction `{}`.", id, t);

            sbds.getMessages().reply(event, "sbds.error", event.getUser())
                    .withPlaceholders("exception", t)
                    .setEphemeral(true)
                    .queue();

        }

    }

    private void processStatic(@NotNull GenericComponentInteractionCreateEvent event) {

        String id = event.getComponentId();
        NamespacedKey key = NamespacedKey.fromString(id);

        User user = event.getUser();
        Member member = event.getMember();

        IRegisteredListener<?> listener = registry.getRegisteredObjects().stream()
                .filter(reg -> reg.getRegistration().key().equals(key))
                .filter(reg -> reg.getEventClass().equals(event.getClass()))
                .findAny()
                .orElse(null);

        if (listener == null) {

            sbds.getMessages().reply(event, "sbds.invalid-interaction", user)
                    .withPlaceholders("id", id)
                    .setEphemeral(true)
                    .queue();

            return;
        }

        Permission permission = listener.getPermission();
        if (member != null && permission != null && !sbds.getPermissionManager().hasPermission(member, permission)) {

            sbds.getMessages().reply(event, "sbds.no-permission", member)
                    .withPlaceholders("permission", permission)
                    .setEphemeral(true)
                    .queue();

            return;
        }

        callExecutor(listener, event);

    }

    // Вспомогательный метод "захватывает" тип T
    @SuppressWarnings("unchecked")
    private <T extends GenericComponentInteractionCreateEvent> void callExecutor(IRegisteredListener<?> listener, GenericComponentInteractionCreateEvent event) {
        IRegisteredListener<T> castedListener = (IRegisteredListener<T>) listener;
        ComponentInteractionInfo<T> info = new ComponentInteractionInfo<>((T) event, castedListener, sbds);
        castedListener.getCallback().accept(info);
    }

    private void processPending(@NotNull GenericComponentInteractionCreateEvent event) {

        String id = event.getComponentId();

        IPendingInteractionAction pendingAction = pendingInteractions.stream()
                .map(p -> p.getActionByGeneratedId(id))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);

        IPendingInteraction pending = pendingAction != null ? pendingAction.getPending() : null;

        User user = event.getUser();

        if (pending == null || (pending.getUser() != null && !user.equals(pending.getUser()))) {

            sbds.getMessages().reply(event, "sbds.invalid-interaction", user)
                    .withPlaceholders("id", id)
                    .setEphemeral(true)
                    .queue();

            return;

        }

        Objects.requireNonNull(pendingAction, "actionAction == null?");

        if (pendingAction.isExpired()) {

            sbds.getMessages().reply(event, "sbds.invalid-interaction", user)
                    .withPlaceholders("id", id)
                    .setEphemeral(true)
                    .queue();

            return;

        }

        ComponentInteractionRequest.Action<?> action = pendingAction.getAction();
        if (action.expire() == ComponentInteractionRequest.ExpireMode.ALL) {
            pendingInteractions.remove(pending);
        }

        else if (action.expire() == ComponentInteractionRequest.ExpireMode.SINGLE) {
            ((PendingInteractionAction) pendingAction).setExpired(true);
        }

        callExecutor(pending, action, event);

    }

    @SuppressWarnings("unchecked")
    private <T extends GenericComponentInteractionCreateEvent> void callExecutor(
            @NotNull IPendingInteraction pending,
            @NotNull ComponentInteractionRequest.Action<T> action,
            @NotNull GenericComponentInteractionCreateEvent event) {
        ComponentInteractionInfo<T> info = new ComponentInteractionInfo<>((T) event, pending, sbds);
        action.action().accept(info);
    }

    private void timeoutChecker() {

        List<IPendingInteraction> pendingInteractions = new ArrayList<>(this.pendingInteractions);

        long time = System.currentTimeMillis();
        for (IPendingInteraction pending : pendingInteractions) {

            if (pending.getTimestamp() + pending.getTimeout() > time) {
                continue;
            }

            this.pendingInteractions.remove(pending);

            Runnable failureCallback = pending.getFailureCallback();
            if (failureCallback == null) {
                continue;
            }

            try {
                failureCallback.run();
            }

            catch (Throwable t) {
                log.error("An exception was thrown while attempting to run failure callback for interaction `{}`.", pending, t);
            }

        }

    }

    //
    // PENDING INTERACTIONS
    //

    // REG //

    @Override
    public @NotNull IPendingInteraction createPending(@NotNull ComponentInteractionRequest builder) {

        Objects.requireNonNull(builder, "builder == null");
        checkValid();

        Map<String, ComponentInteractionRequest.Action<?>> actions = builder.getActions();
        Map<String, String> generatedIds = new HashMap<>();
        actions.forEach((key, value) -> {
            String id = UUID.randomUUID().toString();
            generatedIds.put(key, id);
        });

        Runnable onExpire = builder.getExpireAction();
        int expireTimeout = builder.getExpireInterval();

        User target = builder.getTarget();

        PendingInteraction pendingInteraction = new PendingInteraction(this, actions, generatedIds, target, onExpire, expireTimeout);
        pendingInteractions.add(pendingInteraction);

        return pendingInteraction;

    }

    @Override
    public @NotNull List<IPendingInteraction> getPendingInteractions() {
        checkValid();
        return new ArrayList<>(pendingInteractions);
    }

    //
    // STATIC LISTENERS
    //

    // REG //

    @SuppressWarnings("unchecked") // <- Дінаху сука бля
    @Override
    public @NotNull <event extends GenericComponentInteractionCreateEvent> IRegisteredListener<event> registerListener(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event>> executor,
            @Nullable Permission permission
    ) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(clazz, "clazz == null");
        Objects.requireNonNull(executor, "executor == null");
        checkValid();

        RegisteredListener<event> listener = new RegisteredListener<>(this, clazz, executor, permission);
        listener.registration = (Registration<IRegisteredListener<event>>) (Registration<?>) registry.register(module, name, listener);

        return listener;

    }

    // UNREG //

    @Override
    public boolean unregisterListener(@NotNull IRegisteredListener<?> reg) {
        checkValid();
        return registry.unregister(reg) != null;
    }

    // GETTERS //

    @Override
    public @Nullable IRegisteredListener<?> getRegisteredListener(@NotNull NamespacedKey key) {
        checkValid();
        return registry.getRegistrationAsObject(key);
    }

    @Override
    public @NotNull List<IRegisteredListener<?>> getRegisteredListeners() {
        checkValid();
        return registry.getRegisteredObjects();
    }


    //
    // RECORDS
    //

    public static class RegisteredListener<event extends GenericComponentInteractionCreateEvent> implements IComponentInteractionManager.IRegisteredListener<event> {

        private final ComponentInteractionManager manager;

        private Registration<IRegisteredListener<event>> registration;

        private final Class<event> clazz;

        private final Consumer<ComponentInteractionInfo<event>> executor;

        private final @Nullable Permission permission;


        public RegisteredListener(
                @NotNull ComponentInteractionManager manager,
                @NotNull Class<event> clazz,
                @NotNull Consumer<ComponentInteractionInfo<event>> executor,
                @Nullable Permission permission
        ) {
            this.manager = manager;
            this.clazz = clazz;
            this.executor = executor;
            this.permission = permission;
        }


        @Override
        public @NotNull Registration<IRegisteredListener<event>> getRegistration() {
            return registration;
        }

        @Override
        public @NotNull Class<event> getEventClass() {
            return clazz;
        }

        @Override
        public @NotNull Consumer<ComponentInteractionInfo<event>> getCallback() {
            return executor;
        }

        @Override
        public @Nullable Permission getPermission() {
            return permission;
        }


        @Override
        public @NotNull IComponentInteractionManager getManager() {
            return manager;
        }

        @Override
        public boolean isEphemeral() {
            return true;
        }

    }

    public static class PendingInteraction extends Valid implements IComponentInteractionManager.IPendingInteraction {

        private final ComponentInteractionManager manager;

        private final @Nullable User user;

        private final Map<String, IPendingInteractionAction> actionMap = new HashMap<>();

        private final Map<String, String> generatedIdsMap = new HashMap<>();

        private final @Nullable Runnable failureCallback;

        private final long timestamp;

        private final int timeout;


        public PendingInteraction(
                @NotNull ComponentInteractionManager manager,
                @NotNull Map<String, ComponentInteractionRequest.Action<?>> actionMap,
                @NotNull Map<String, String> generatedIdsMap,
                @Nullable User user,
                @Nullable Runnable failureCallback,
                int timeout
        ) {

            this.manager = manager;

            this.actionMap.putAll(actionMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> (IPendingInteractionAction) new PendingInteractionAction(entry.getValue(), this)))
            );
            this.generatedIdsMap.putAll(generatedIdsMap);

            this.user = user;

            this.failureCallback = failureCallback;
            this.timestamp = System.currentTimeMillis();
            this.timeout = timeout * 1000;

        }


        @Override
        public @Nullable User getUser() {
            return user;
        }

        @Override
        public @Nullable Runnable getFailureCallback() {
            return failureCallback;
        }

        @Override
        public @NotNull Map<String, IPendingInteractionAction> getActions() {
            return new HashMap<>(actionMap);
        }

        @Override
        public @NotNull Map<String, String> getGeneratedIds() {
            return new HashMap<>(generatedIdsMap);
        }


        @Override
        public int getTimeout() {
            return timeout;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }


        @Override
        public @NotNull IComponentInteractionManager getManager() {
            return manager;
        }

        @Override
        public boolean isEphemeral() {
            return true;
        }

    }

    public static class PendingInteractionAction implements IPendingInteractionAction {

        private final ComponentInteractionRequest.Action<?> action;

        private final IPendingInteraction pendingInteraction;

        private boolean expired = false;


        public PendingInteractionAction(@NotNull ComponentInteractionRequest.Action<?> action, @NotNull IPendingInteraction pendingInteraction) {
            this.action = action;
            this.pendingInteraction = pendingInteraction;
        }

        @Override
        public @NotNull ComponentInteractionRequest.Action<?> getAction() {
            return action;
        }

        @Override
        public @NotNull IPendingInteraction getPending() {
            return pendingInteraction;
        }


        @Override
        public boolean isExpired() {
            return expired;
        }

        public void setExpired(boolean expired) {
            this.expired = expired;
        }

    }

}
