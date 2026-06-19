package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.permissions.Permission;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.IManager;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface IComponentInteractionManager extends IManager {

    //
    // PENDING INTERACTIONS
    //

    @NotNull IPendingInteraction createPending(@NotNull ComponentInteractionRequest builder);

    @NotNull List<IPendingInteraction> getPendingInteractions();

    //
    // STATIC COMPONENTS
    //

    // REG //

    <event extends GenericComponentInteractionCreateEvent> @NotNull IRegisteredListener<event> registerListener(
            @NotNull IModule module,
            @NotNull String name,
            @Nullable Permission permission,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event, IRegisteredListener<event>>> executor
    );

    default <event extends GenericComponentInteractionCreateEvent> @NotNull IRegisteredListener<event> registerListener(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event, IRegisteredListener<event>>> executor
    ) {
        return registerListener(module, name, null, clazz, executor);
    }

    default <event extends GenericComponentInteractionCreateEvent> @NotNull IRegisteredListener<event> registerListener(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event, IRegisteredListener<event>>> executor,
            @Nullable Permission permission
    ) {
        return registerListener(module.getModule(), name, permission, clazz, executor);
    }

    default <event extends GenericComponentInteractionCreateEvent> @NotNull IRegisteredListener<event> registerListener(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event, IRegisteredListener<event>>> executor
    ) {
        return registerListener(module.getModule(), name, null, clazz, executor);
    }

    // UNREG //

    boolean unregisterListener(@NotNull IRegisteredListener<?> reg);

    // GETTERS //

    @Nullable IRegisteredListener<?> getRegisteredListener(@NotNull NamespacedKey key);

    default @Nullable IRegisteredListener<?> getRegisteredListener(@NotNull String key) {
        return getRegisteredListener(NamespacedKey.fromString(key));
    }

    @NotNull List<IRegisteredListener<?>> getRegisteredListeners();

    //
    // FUCKING PIZDEC
    // ABOBA ABOBA ABOBA
    // AMOGUS AMOGUS ABOBUS
    // 67 67 67 69 69 69 34 34 34
    // Злий динозаврик позаду тебе! Він проковтне тебе і ти більше не будеш писати код! Бу-га-га-га!
    //

    interface IRegisteredComponent {

        @NotNull IComponentInteractionManager getManager();

        boolean isEphemeral();

    }

    interface IRegisteredListener<event extends GenericComponentInteractionCreateEvent> extends IRegisteredComponent {

        @NotNull Registration<IRegisteredListener<event>> getRegistration();

        @NotNull Class<event> getEventClass();

        @Nullable Permission getPermission();


        @NotNull Consumer<ComponentInteractionInfo<event, IRegisteredListener<event>>> getCallback();

    }

    interface IPendingInteraction extends IRegisteredComponent, IValid {

        @Nullable User getUser();


        long getTimestamp();

        int getTimeout();


        @Nullable Runnable getFailureCallback();

        @NotNull Map<String, IPendingInteractionAction> getActions();

        @NotNull Map<String, String> getGeneratedIds();

        default @Nullable IPendingInteractionAction getActionByGeneratedId(@NotNull String id) {

            String name = getGeneratedIds().entrySet().stream()
                    .filter(entry -> entry.getValue().equals(id))
                    .map(Map.Entry::getKey)
                    .findAny()
                    .orElse(null);

            if (name == null) {
                return null;
            }

            return getActions().get(name);

        }


    }

    interface IPendingInteractionAction {

        @NotNull ComponentInteractionRequest.Action<?> getAction();

        @NotNull IPendingInteraction getPending();

        boolean isExpired();

    }

}
