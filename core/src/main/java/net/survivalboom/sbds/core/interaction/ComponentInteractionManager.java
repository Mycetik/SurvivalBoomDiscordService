package net.survivalboom.sbds.core.interaction;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.api.interaction.ComponentInteractionInfo;
import net.survivalboom.sbds.api.interaction.IComponentInteractionManager;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.permissions.Permission;
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

public class ComponentInteractionManager extends Manager implements IComponentInteractionManager, EventListener {

    private static final Logger log = LoggerFactory.getLogger(ComponentInteractionManager.class.getSimpleName());


    private final SBDS sbds;

    private final InternalRegistrationManager<IRegisteredListener<?>> registry;

    private final Map<String, IPendingInteraction<?>> pendingInteractions = new HashMap<>();

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

            sbds.getMessages().reply(event, "sbds.invalid-interaction", event.getUser())
                    .withPlaceholders("{exception}", t)
                    .send()
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
                    .withPlaceholders("{id}", id)
                    .send()
                    .setEphemeral(true)
                    .queue();

            return;
        }

        Permission permission = listener.getPermission();
        if (member != null && permission != null && !sbds.getPermissionManager().hasPermission(member, permission)) {

            sbds.getMessages().reply(event, "sbds.no-permission", member)
                    .withPlaceholders("{permission}", permission)
                    .send()
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
        ComponentInteractionInfo<T> info = new ComponentInteractionInfo<>((T) event, sbds);
        castedListener.getExecutor().accept(info);
    }

    private void processPending(@NotNull GenericComponentInteractionCreateEvent event) {

        String id = event.getComponentId();
        IPendingInteraction<?> pending = pendingInteractions.get(id);

        User user = event.getUser();

        if (pending == null || (pending.getUser() != null && !user.equals(pending.getUser()))) {

            sbds.getMessages().reply(event, "sbds.invalid-interaction", user)
                    .withPlaceholders("{id}", id)
                    .send()
                    .setEphemeral(true)
                    .queue();

            return;
        }

        pendingInteractions.remove(id);

        callExecutor(pending, event);

    }

    @SuppressWarnings("unchecked")
    private <T extends GenericComponentInteractionCreateEvent> void callExecutor(IPendingInteraction<?> pending, GenericComponentInteractionCreateEvent event) {
        IPendingInteraction<T> castedPending = (IPendingInteraction<T>) pending;
        ComponentInteractionInfo<T> info = new ComponentInteractionInfo<>((T) event, sbds);
        castedPending.getSuccessCallback().accept(info);
    }

    private void timeoutChecker() {

        var pendingInteractions = new HashMap<>(this.pendingInteractions);

        long time = System.currentTimeMillis();
        for (var entry : pendingInteractions.entrySet()) {

            String id = entry.getKey();
            IPendingInteraction<?> interaction = entry.getValue();

            if (interaction.getTimestamp() + interaction.getTimeout() > time) {
                continue;
            }

            this.pendingInteractions.remove(id);
            log.info("Pending interaction `{}` expired.", id);

            Runnable failureCallback = interaction.getFailureCallback();
            if (failureCallback == null) {
                continue;
            }

            try {
                failureCallback.run();
            }

            catch (Throwable t) {
                log.error("An exception was thrown while attempting to run failure callback for interaction `{}`.", id, t);
            }

        }

    }

    //
    // PENDING INTERACTIONS
    //

    // REG //

    @Override
    public @NotNull <event extends GenericComponentInteractionCreateEvent> IPendingInteraction<event> registerPendingInteraction(
            @NotNull String id,
            @Nullable User user,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event>> successCallback,
            @Nullable Runnable failureCallback,
            int timeout
    ) {


        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(clazz, "clazz == null");
        Objects.requireNonNull(successCallback, "successCallback == null");
        Objects.requireNonNull(failureCallback, "failureCallback == null");
        checkValid();

        if (pendingInteractions.containsKey(id)) {
            throw new IllegalArgumentException("Id `" + id + "` already exists");
        }

        PendingInteraction<event> pendingInteraction = new PendingInteraction<>(this, id, user, successCallback, failureCallback, timeout);
        pendingInteractions.put(id, pendingInteraction);

        return pendingInteraction;

    }

    // UNREG //

    @Override
    public @Nullable IPendingInteraction<?> forgetPendingInteraction(@NotNull String id) {
        checkValid();
        return pendingInteractions.remove(id);
    }

    // GETTERS //

    @Override
    public @Nullable IPendingInteraction<?> getPendingUInteraction(@Nullable String id) {
        checkValid();
        return pendingInteractions.get(id);
    }

    @Override
    public @NotNull List<IPendingInteraction<?>> getPendingInteractions() {
        checkValid();
        return new ArrayList<>(pendingInteractions.values());
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
        public @NotNull Consumer<ComponentInteractionInfo<event>> getExecutor() {
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

    }

    public static class PendingInteraction<event extends GenericComponentInteractionCreateEvent> implements IComponentInteractionManager.IPendingInteraction<event> {

        private final ComponentInteractionManager manager;

        private final String id;

        private final @Nullable User user;

        private final Consumer<ComponentInteractionInfo<event>> successCallback;

        private final @Nullable Runnable failureCallback;

        private final long timestamp;

        private final int timeout;


        public PendingInteraction(
                @NotNull ComponentInteractionManager manager,
                @NotNull String id,
                @Nullable User user,
                @NotNull Consumer<ComponentInteractionInfo<event>> successCallback,
                @Nullable Runnable failureCallback,
                int timeout
        ) {

            this.manager = manager;

            this.id = id;
            this.user = user;

            this.successCallback = successCallback;
            this.failureCallback = failureCallback;

            this.timestamp = System.currentTimeMillis();
            this.timeout = timeout;

        }


        @Override
        public @NotNull String getId() {
            return id;
        }

        @Override
        public @Nullable User getUser() {
            return user;
        }

        @Override
        public @NotNull Consumer<ComponentInteractionInfo<event>> getSuccessCallback() {
            return successCallback;
        }

        @Override
        public @Nullable Runnable getFailureCallback() {
            return failureCallback;
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

    }

}
