package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModalActionBuilder {

    private final IModalInteractionManager modalInteractionManager;

    private final IMessages messages;

    private final User user;

    private final NamespacedKey key;

    private final IModalCallback callback;


    private Placeholders placeholders;

    private Consumer<ModalInteractionInfo> onSuccess;

    private Runnable onFail;

    private int timeout = 120000;


    public ModalActionBuilder(@NotNull IModalInteractionManager modalInteractionManager, @NotNull IModalCallback callback, @NotNull NamespacedKey key) {
        this.modalInteractionManager = modalInteractionManager;
        this.messages = modalInteractionManager.getMessages();
        this.user = callback.getUser();
        this.callback = callback;
        this.key = key;
    }

    public @NotNull ModalCallbackAction send() {

        Objects.requireNonNull(onSuccess, "onSuccess == null");

        IModalInteractionManager.IRegisteredModal registeredModal = modalInteractionManager.getModal(key);
        if (registeredModal == null) throw new IllegalArgumentException("No modal found by key " + key);

        ModalTemplate template = registeredModal.template();

        String name = template.name();
        String id = Objects.requireNonNullElse(name, UUID.randomUUID().toString());

        Function<String, String> parser = s -> messages.parse(s, key -> messages.getMessage(key, user, true), placeholders);;
        Object modal = template.create(id, parser);

        if (name == null) {
            modalInteractionManager.registerPendingInteraction(id, user, onSuccess, onFail, timeout);
        }

        return callback.replyModal((net.dv8tion.jda.api.modals.Modal) modal);

    }

    public void queue() {
        send().queue();
    }


    public @NotNull ModalActionBuilder withPlaceholders(@Nullable Placeholders placeholders) {
        this.placeholders = placeholders;
        return this;
    }

    public @NotNull ModalActionBuilder onSuccess(@NotNull Consumer<ModalInteractionInfo> consumer) {
        this.onSuccess = consumer;
        return this;
    }

    public @NotNull ModalActionBuilder onFail(@Nullable Runnable runnable) {
        this.onFail = runnable;
        return this;
    }

    public @NotNull ModalActionBuilder withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }


}
