package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
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

    // BUTTONS //

    default @NotNull IRegisteredListener<ButtonInteractionEvent> registerButton(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<ButtonInteractionEvent>> executor,
            @Nullable Permission permission
    ) {
        return registerListener(module, name, ButtonInteractionEvent.class, executor, permission);
    }

    default @NotNull IRegisteredListener<ButtonInteractionEvent> registerButton(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<ButtonInteractionEvent>> executor
    ) {
        return registerListener(module, name, ButtonInteractionEvent.class, executor, null);
    }


    default @NotNull IRegisteredListener<ButtonInteractionEvent> registerButton(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<ButtonInteractionEvent>> executor,
            @Nullable Permission permission
    ) {
        return registerListener(module, name, ButtonInteractionEvent.class, executor, permission);
    }

    default @NotNull IRegisteredListener<ButtonInteractionEvent> registerButton(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<ButtonInteractionEvent>> executor
    ) {
        return registerListener(module, name, ButtonInteractionEvent.class, executor, null);
    }

    // ENTITY SELECT //

    default @NotNull IRegisteredListener<EntitySelectInteractionEvent> registerEntityDropdown(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<EntitySelectInteractionEvent>> executor,
            @Nullable Permission permission
    ) {
        return registerListener(module, name, EntitySelectInteractionEvent.class, executor, permission);
    }

    default @NotNull IRegisteredListener<EntitySelectInteractionEvent> registerEntityDropdown(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<EntitySelectInteractionEvent>> executor
    ) {
        return registerListener(module, name, EntitySelectInteractionEvent.class, executor, null);
    }


    default @NotNull IRegisteredListener<EntitySelectInteractionEvent> registerEntityDropdown(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<EntitySelectInteractionEvent>> executor,
            @Nullable Permission permission
    ) {
        return registerListener(module, name, EntitySelectInteractionEvent.class, executor, permission);
    }

    default @NotNull IRegisteredListener<EntitySelectInteractionEvent> registerEntityDropdown(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<EntitySelectInteractionEvent>> executor
    ) {
        return registerListener(module, name, EntitySelectInteractionEvent.class, executor, null);
    }

    // STRING SELECT //

    default @NotNull IRegisteredListener<StringSelectInteractionEvent> registerStringDropdown(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<StringSelectInteractionEvent>> executor,
            @Nullable Permission permission
    ) {
        return registerListener(module, name, StringSelectInteractionEvent.class, executor, permission);
    }

    default @NotNull IRegisteredListener<StringSelectInteractionEvent> registerStringDropdown(
            @NotNull IModule module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<StringSelectInteractionEvent>> executor
    ) {
        return registerListener(module, name, StringSelectInteractionEvent.class, executor, null);
    }


    default @NotNull IRegisteredListener<StringSelectInteractionEvent> registerStringDropdown(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<StringSelectInteractionEvent>> executor,
            @Nullable Permission permission
    ) {
        return registerListener(module, name, StringSelectInteractionEvent.class, executor, permission);
    }

    default @NotNull IRegisteredListener<StringSelectInteractionEvent> registerStringDropdown(
            @NotNull ModuleMain module,
            @NotNull String name,
            @NotNull Consumer<ComponentInteractionInfo<StringSelectInteractionEvent>> executor
    ) {
        return registerListener(module, name, StringSelectInteractionEvent.class, executor, null);
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


        default @NotNull IRegisteredListener<?> asListener() {
            return (IRegisteredListener<?>) this;
        }

        default @NotNull IPendingInteraction asPending() {
            return (IPendingInteraction) this;
        }

    }

    interface IRegisteredListener<event extends GenericComponentInteractionCreateEvent> extends IRegisteredComponent {

        @NotNull Registration<IRegisteredListener<event>> getRegistration();

        @NotNull Class<event> getEventClass();

        @Nullable Permission getPermission();


        @NotNull Consumer<ComponentInteractionInfo<event>> getCallback();

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
