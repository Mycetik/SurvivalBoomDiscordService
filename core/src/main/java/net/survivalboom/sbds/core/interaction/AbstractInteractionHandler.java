package net.survivalboom.sbds.core.interaction;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.interaction.InteractionManager;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.modules.Module;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractInteractionHandler<T, E extends IReplyCallback> extends Manager implements InteractionManager<T>, Listener {

    protected final SBDS sbds;

    protected final Scheduler scheduler;

    protected SchedulerTask task;


    protected final String name;

    protected final Logger logger;


    protected final Set<PendingInteraction<T>> pendingInteractions = new HashSet<>();

    protected final Set<RegisteredInteractionListener<T>> registeredInteractionListeners = new HashSet<>();


    public AbstractInteractionHandler(@NotNull String name, @NotNull SBDS sbds) {
        this.name = name;
        this.sbds = sbds;
        this.logger = LoggerFactory.getLogger(name);
        this.scheduler = sbds.getScheduler();
    }

    @Override
    protected void init0() {
        task = scheduler.schedule0(null, null, task -> this.task(), 100, 100);
        sbds.getEventManager().registerEvents0(null, this);
    }

    @Override
    protected void shutdown0() {
        sbds.getEventManager().unregisterEvents(this);
        task.cancelAndWait(200, true);
        task = null;
    }

    private void task() {

        long currentTime = System.currentTimeMillis();
        for (PendingInteraction<T> pending : pendingInteractions) {

            if (pending.timestamp < 1) continue;
            if (pending.timestamp + pending.timeout > currentTime) continue;

            pendingInteractions.removeIf(p -> p.id.equals(pending.id));
            Runnable onFail = pending.onFail;

            if (onFail == null) return;

            onFail.run();

        }

    }

    @Override
    public void registerPendingInteraction(@NotNull String id, @Nullable User user, @NotNull Consumer<T> onSuccess, @Nullable Runnable onFail, long timeout) {

        checkValid();

        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(onSuccess, "onSuccess == null");

        if (pendingInteractions.stream().anyMatch(p -> p.id.equals(id))) {
            throw new IllegalArgumentException("Pending interaction with id `" + id + "` already exists");
        }

        PendingInteraction<T> pending = new PendingInteraction<>(id, onSuccess, onFail, user, System.currentTimeMillis(), timeout);
        pendingInteractions.add(pending);

    }

    @Override
    public @NotNull RegisteredInteractionListener<T> registerListener(@NotNull IModule iModule, @NotNull String name, @NotNull Consumer<T> consumer, @Nullable String permission) {

        Objects.requireNonNull(iModule, "module == null");

        return registerListener0(iModule, name, consumer, permission);

    }

    public @NotNull RegisteredInteractionListener<T> registerListener0(@Nullable IModule iModule, @NotNull String name, @NotNull Consumer<T> consumer, @Nullable String permission) {

        Objects.requireNonNull(name, "name == null");

        NamespacedKey key = iModule != null ? NamespacedKey.fromModule(iModule, name) : NamespacedKey.sbds(name);

        return registerListener1(iModule, key, consumer, permission);

    }

    public @NotNull RegisteredInteractionListener<T> registerListener1(@Nullable IModule imodule, @NotNull NamespacedKey key, @NotNull Consumer<T> consumer, @Nullable String permission) {

        checkValid();

        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(consumer, "consumer == null");

        Module module = imodule != null ? sbds.getModuleManager().checkModuleEnabled(imodule, "Disabled module tried to register an interaction listener") : null;

        if (registeredInteractionListeners.stream().anyMatch(b -> b.key().equals(key))) {
            throw new IllegalArgumentException("Interaction listener with name `" + key + "` already exists");
        }

        RegisteredInteractionListener<T> button = new RegisteredInteractionListener<>(module, key, consumer, permission);
        registeredInteractionListeners.add(button);

        if (module != null) {
            module.getRegistration().add(name + "-" + key, () -> this.unregisterListener1(module, key));
        }

        return button;

    }

    @Override
    public void unregisterListener(@NotNull IModule module, @NotNull String name) {

        Objects.requireNonNull(module, "module == null");

        unregisterListener0(module, name);

    }

    public void unregisterListener0(@Nullable IModule imodule, @NotNull String name) {

        Objects.requireNonNull(name, "name == null");

        NamespacedKey key = imodule != null ? NamespacedKey.fromModule(imodule, name) : NamespacedKey.sbds(name);

        Module module = imodule != null ? sbds.getModuleManager().checkModuleValid(imodule) : null;

        unregisterListener1(module, key);

    }

    public void unregisterListener1(@Nullable Module module, @NotNull NamespacedKey key) {

        checkValid();

        registeredInteractionListeners.removeIf(b -> b.key().equals(key));

    }


    protected void onEvent(E event) {

        try {

            String id = getIdFromEvent(event);
            if (id == null) {
                return;
            }

            if (!id.contains(":")) {
                processPending(event, id);
                return;
            }

            processStatic(event, id);

        }

        catch (Throwable t) {
            logger.error("An exception was thrown in interaction processor.", t);
            sbds.getMessages().reply(event, "sbds.error", event.getUser()).withPlaceholders(Placeholders.of("{EXCEPTION}", t)).send().setEphemeral(true).queue();
        }

    }

    private void processStatic(@NotNull E event, @NotNull String id) {

        NamespacedKey key = NamespacedKey.fromString(id);

        RegisteredInteractionListener<T> button = registeredInteractionListeners.stream().filter(b -> b.key.equals(key)).findAny().orElse(null);
        if (button == null) {
            sbds.getMessages().reply(event, "sbds.invalid-interaction", event.getUser()).withPlaceholders(Placeholders.of("{ID}", id)).send().setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();
        if (button.permission != null && member != null && !sbds.getPermissionManager().hasPermission(member, button.permission, false)) {
            sbds.getMessages().reply(event, "sbds.no-permission", event.getUser()).withPlaceholders("{PERMISSION}", button.permission).send().setEphemeral(true).queue();
            return;
        }

        button.consumer.accept(createInteractionInfo(event));

    }

    private void processPending(@NotNull E event, @NotNull String id) {

        PendingInteraction<T> pending = pendingInteractions.stream().filter(p -> p.id.equals(id)).findAny().orElse(null);
        if (pending == null || !event.getUser().equals(pending.user)) {
            sbds.getMessages().reply(event, "sbds.invalid-interaction", event.getUser()).withPlaceholders(Placeholders.of("{ID}", id)).send().setEphemeral(true).queue();
            return;
        }

        pendingInteractions.removeIf(p -> p.id.equals(id));
        pending.onSuccess.accept(createInteractionInfo(event));

    }


    protected abstract @Nullable String getIdFromEvent(E event);

    protected abstract @NotNull T createInteractionInfo(E event);


    protected record PendingInteraction<T>(
            @NotNull String id,
            @NotNull Consumer<T> onSuccess,
            @Nullable Runnable onFail,
            @Nullable User user,
            long timestamp,
            long timeout
    ) {}

    public record RegisteredInteractionListener<T>(
            @Nullable Module module,
            @NotNull NamespacedKey key,
            @NotNull Consumer<T> consumer,
            @Nullable String permission
    ) implements IRegisteredListener {}

}
