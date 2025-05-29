package net.survivalboom.sbds.core.interaction.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.interaction.button.IButtonInteractionManager;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.scheduler.Scheduler;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

// TODO: Зробити нормальну абстракцію. В нас тут 3 класи з одним і тим же функціоналом й одним тим же кодом.
public class ButtonInteractionManager extends Manager implements IButtonInteractionManager, Listener {

    private static final Logger log = LoggerFactory.getLogger(ButtonInteractionManager.class.getSimpleName());

    private final SBDS sbds;

    private final Scheduler scheduler;

    private SchedulerTask task;


    private final Set<PendingInteraction> pendingInteractions = new HashSet<>();

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

        PendingInteraction pending = pendingInteractions.stream().filter(p -> p.id.equals(id)).findAny().orElse(null);
        if (pending == null) {
            event.reply("Button with id `" + id + "` not found!").setEphemeral(true).queue();
            return;
        }

        pendingInteractions.removeIf(p -> p.id.equals(id));
        pending.onSuccess.accept(new ButtonInteractionInfo(sbds, event));

    }


    record PendingInteraction(@NotNull String id, @NotNull Consumer<ButtonInteractionInfo> onSuccess, @NotNull Runnable onFail, long timestamp, long timeout) {}



}
