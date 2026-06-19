package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ComponentInteractionRequest {

    private final Map<String, Action<?>> actionMap = new HashMap<>();

    private final int expireInterval;

    private final Runnable expireAction;

    private final @Nullable User target;


    public ComponentInteractionRequest(@NotNull Collection<Action<?>> actions, int expireInterval, @Nullable Runnable expireAction, @Nullable User target) {

        Objects.requireNonNull(actions, "actions == null");
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("actions is empty");
        }

        this.expireInterval = expireInterval;
        this.expireAction = expireAction;

        actions.forEach(action -> actionMap.put(action.name, action));

        this.target = target;

    }

    public @NotNull Map<String, Action<?>> getActions() {
        return new HashMap<>(actionMap);
    }

    public int getExpireInterval() {
        return expireInterval;
    }

    public @Nullable Runnable getExpireAction() {
        return expireAction;
    }

    public @Nullable User getTarget() {
        return target;
    }

    public record Action<
            event extends GenericComponentInteractionCreateEvent
    >(
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event, IComponentInteractionManager.IPendingInteraction>> action,
            @NotNull ExpireMode expire
    ) {}

    public enum ExpireMode {
        OFF,
        SINGLE,
        ALL
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Action<?>> actions = new ArrayList<>();

        private int expireInterval = 300;

        private Runnable expireAction = null;

        private User target = null;

        private Builder() {}

        // EXPIRE ACTION //

        public @NotNull Builder setExpireAction(@Nullable Runnable runnable) {
            this.expireAction = runnable;
            return this;
        }

        public Runnable getExpireAction() {
            return expireAction;
        }

        // EXPIRE TIME //

        public @NotNull Builder setExpireInterval(int time) {
            this.expireInterval = time;
            return this;
        }

        public int getExpireInterval() {
            return expireInterval;
        }

        // TARGET //

        public @NotNull Builder setTarget(@Nullable User target) {
            this.target = target;
            return this;
        }

        public User getTarget() {
            return target;
        }

        //
        // ACTIONS
        //

        // GENERIC //

        public <T extends GenericComponentInteractionCreateEvent> @NotNull Builder addAction(
                @NotNull String name,
                @NotNull Class<T> clazz,
                @NotNull ExpireMode mode,
                @NotNull Consumer<ComponentInteractionInfo<T, IComponentInteractionManager.IPendingInteraction>> action
        ) {
            this.actions.add(new Action<>(name, clazz, action, mode));
            return this;
        }

        public @NotNull Builder addAction(@NotNull Action<?> action) {
            this.actions.add(action);;
            return this;
        }

        public @NotNull Builder setActions(@Nullable Collection<Action<?>> actions) {

            this.actions.clear();

            if (actions != null) {
                this.actions.addAll(actions);
            }

            return this;

        }

        public @NotNull List<Action<?>> getActions() {
            return new ArrayList<>(actions);
        }

        // BUTTON //

        public @NotNull Builder addButton(
                @NotNull String name,
                @NotNull ExpireMode mode,
                @NotNull Consumer<ComponentInteractionInfo<ButtonInteractionEvent, IComponentInteractionManager.IPendingInteraction>> callback
        ) {
            actions.add(new Action<>(name, ButtonInteractionEvent.class, callback, mode));
            return this;
        }

        // STRING DROPDOWN //

        public @NotNull Builder addStringDropdown(
                @NotNull String name,
                @NotNull ExpireMode mode,
                @NotNull Consumer<ComponentInteractionInfo<StringSelectInteractionEvent, IComponentInteractionManager.IPendingInteraction>> callback
        ) {
            actions.add(new Action<>(name, StringSelectInteractionEvent.class, callback, mode));
            return this;
        }

        // ENTITY DROPDOWN //

        public @NotNull Builder addEntityDropdown(
                @NotNull String name,
                @NotNull ExpireMode mode,
                @NotNull Consumer<ComponentInteractionInfo<EntitySelectInteractionEvent, IComponentInteractionManager.IPendingInteraction>> callback
        ) {
            actions.add(new Action<>(name, EntitySelectInteractionEvent.class, callback, mode));
            return this;
        }

        // BUILD //

        public @NotNull ComponentInteractionRequest build() {
            return new ComponentInteractionRequest(actions, expireInterval, expireAction, target);
        }

    }

}
