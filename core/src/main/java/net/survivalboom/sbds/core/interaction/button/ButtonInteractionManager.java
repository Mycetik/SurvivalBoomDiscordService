package net.survivalboom.sbds.core.interaction.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.interaction.button.IButtonInteractionManager;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.modules.Module;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

// TODO: Зробити нормальну абстракцію. В нас тут 3 класи з одним і тим же функціоналом й одним тим же кодом.
public class ButtonInteractionManager extends Manager implements IButtonInteractionManager, Listener {

    private final SBDS sbds;

    private final Scheduler scheduler;

    private SchedulerTask task;

    private final Set<PendingInteraction> pendingInteractions = new HashSet<>();

    private final Set<RegisteredButton> registeredButtons = new HashSet<>();

    public ButtonInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
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
        for (PendingInteraction pending : pendingInteractions) {

            if (pending.timestamp + pending.timeout > currentTime) continue;

            pendingInteractions.removeIf(p -> p.id.equals(pending.id));
            pending.onFail.run();

        }

    }

    @Override
    public @NotNull RegisteredButton registerButton(@NotNull IModule iModule, @NotNull String name, @NotNull Consumer<ButtonInteractionInfo> consumer) {

        Objects.requireNonNull(iModule, "module == null");

        return registerButton0(iModule, name, consumer);

    }

    public @NotNull RegisteredButton registerButton0(@Nullable IModule iModule, @NotNull String name, @NotNull Consumer<ButtonInteractionInfo> consumer) {

        Objects.requireNonNull(name, "name == null");

        NamespacedKey key = iModule != null ? NamespacedKey.fromModule(iModule, name) : NamespacedKey.sbds(name);

        return registerButton1(iModule, key, consumer);

    }

    public @NotNull RegisteredButton registerButton1(@Nullable IModule imodule, @NotNull NamespacedKey key, @NotNull Consumer<ButtonInteractionInfo> consumer) {

        checkValid();

        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(consumer, "consumer == null");

        Module module = imodule != null ? sbds.getModuleManager().checkModuleEnabled(imodule, "Disabled module tried to register a button") : null;

        if (registeredButtons.stream().anyMatch(b -> b.key.equals(key))) {
            throw new IllegalArgumentException("Button with name `" + key + "` already exists");
        }

        RegisteredButton button = new RegisteredButton(module, key, consumer);
        registeredButtons.add(button);

        if (module != null) {
            module.getRegistration().add("Button-" + key, () -> this.unregisterButton1(module, key));
        }

        return button;

    }

    public void unregisterButton(@NotNull IModule module, @NotNull String name) {

        Objects.requireNonNull(module, "module == null");

        unregisterButton0(module, name);

    }

    public void unregisterButton0(@Nullable IModule imodule, @NotNull String name) {

        Objects.requireNonNull(name, "name == null");

        NamespacedKey key = imodule != null ? NamespacedKey.fromModule(imodule, name) : NamespacedKey.sbds(name);

        Module module = imodule != null ? sbds.getModuleManager().checkModuleValid(imodule) : null;

        unregisterButton1(module, key);

    }

    public void unregisterButton1(@Nullable Module module, @NotNull NamespacedKey key) {

        checkValid();

        registeredButtons.removeIf(b -> b.key.equals(key));

    }

    public void registerPendingInteraction(@NotNull String id, @NotNull Consumer<ButtonInteractionInfo> onSuccess, @NotNull Runnable onFail, long timeout) {

        checkValid();

        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(onSuccess, "onSuccess == null");
        Objects.requireNonNull(onFail, "onFail == null");

        if (pendingInteractions.stream().anyMatch(p -> p.id.equals(id))) {
            throw new IllegalArgumentException("Pending interaction with id `" + id + "` already exists");
        }

        PendingInteraction pending = new PendingInteraction(id, onSuccess, onFail, System.currentTimeMillis(), timeout);
        pendingInteractions.add(pending);

    }


    @EventHandler
    public void onButtonClick(@NotNull ButtonInteractionEvent event) {

        String id = event.getButton().getId();
        if (id == null) {
            return;
        }

        if (!id.contains(":")) {
            processPendingButton(event);
            return;
        }

        processStaticButton(event);

    }

    private void processStaticButton(@NotNull ButtonInteractionEvent event) {

        String id = event.getButton().getId();
        Objects.requireNonNull(id);

        NamespacedKey key;

        try {
            key = NamespacedKey.fromString(id);
        }

        catch (IllegalArgumentException e) {
            event.reply("Invalid button NamespacedKey `" + id + "`. \n`" + e + "`").queue();
            return;
        }


        RegisteredButton button = registeredButtons.stream().filter(b -> b.key.equals(key)).findAny().orElse(null);
        if (button == null) {
            event.reply("Static button with key `" + key + "` not found!").setEphemeral(true).queue();
            return;
        }

        button.consumer.accept(new ButtonInteractionInfo(sbds, event));

    }

    private void processPendingButton(@NotNull ButtonInteractionEvent event) {

        String id = event.getButton().getId();

        PendingInteraction pending = pendingInteractions.stream().filter(p -> p.id.equals(id)).findAny().orElse(null);
        if (pending == null) {
            event.reply("Button with id `" + id + "` not found!").setEphemeral(true).queue();
            return;
        }

        pendingInteractions.removeIf(p -> p.id.equals(id));
        pending.onSuccess.accept(new ButtonInteractionInfo(sbds, event));

    }


    private record PendingInteraction(@NotNull String id, @NotNull Consumer<ButtonInteractionInfo> onSuccess, @NotNull Runnable onFail, long timestamp, long timeout) {}

    public record RegisteredButton(@Nullable Module module, @NotNull NamespacedKey key, @NotNull Consumer<ButtonInteractionInfo> consumer) implements IRegisteredButton {}



}
