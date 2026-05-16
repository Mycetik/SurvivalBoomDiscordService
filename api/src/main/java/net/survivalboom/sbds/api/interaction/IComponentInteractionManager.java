package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.permissions.Permission;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface IComponentInteractionManager extends IManager {

    //
    // PENDING INTERACTIONS
    //

    // REG //

    <event extends GenericComponentInteractionCreateEvent> @NotNull IPendingInteraction<event> registerPendingInteraction(
            @NotNull String id,
            @Nullable User user,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event>> successCallback,
            @Nullable Runnable failureCallback,
            int timeout
    );

    // UNREG //

    @Nullable IPendingInteraction<?> forgetPendingInteraction(@NotNull String id);

    // GETTERS //

    @Nullable IPendingInteraction<?> getPendingUInteraction(@Nullable String id);

    @NotNull List<IPendingInteraction<?>> getPendingInteractions();

    //
    // STATIC COMPONENTS
    //

    // REG //

    <event extends GenericComponentInteractionCreateEvent> @NotNull IRegisteredListener<event> registerListener(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event>> executor,
            @Nullable Permission permission
    );

    default <event extends GenericComponentInteractionCreateEvent> @NotNull IRegisteredListener<event> registerListener(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event>> executor
    ) {
        return registerListener(module, name, clazz, executor, null);
    }

    default <event extends GenericComponentInteractionCreateEvent> @NotNull IRegisteredListener<event> registerListener(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event>> executor,
            @Nullable Permission permission
    ) {
        return registerListener(module.getModule(), name, clazz, executor, permission);
    }

    default <event extends GenericComponentInteractionCreateEvent> @NotNull IRegisteredListener<event> registerListener(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Class<event> clazz,
            @NotNull Consumer<ComponentInteractionInfo<event>> executor
    ) {
        return registerListener(module.getModule(), name, clazz, executor, null);
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

    interface IRegisteredListener<event extends GenericComponentInteractionCreateEvent> {

        @NotNull Registration<IRegisteredListener<event>> getRegistration();

        @NotNull Class<event> getEventClass();

        @NotNull Consumer<ComponentInteractionInfo<event>> getExecutor();

        @Nullable Permission getPermission();


        @NotNull IComponentInteractionManager getManager();

    }

    interface IPendingInteraction<event extends GenericComponentInteractionCreateEvent> {

        @NotNull String getId();

        @Nullable User getUser();

        @NotNull Consumer<ComponentInteractionInfo<event>> getSuccessCallback();

        @Nullable Runnable getFailureCallback();


        long getTimestamp();

        int getTimeout();


        @NotNull IComponentInteractionManager getManager();

    }

}
