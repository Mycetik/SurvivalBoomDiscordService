package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.survivalboom.sbds.api.messages.parsers.LinkedTextParser;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ModalActionBuilder {

    private final IModalInteractionManager modalInteractionManager;

    private final User user;

    private final NamespacedKey key;

    private final IModalCallback callback;


    private final LinkedTextParser.Builder builder;


    private Consumer<ModalInteractionInfo> onSuccess;

    private Runnable onFail;

    private int timeout = 120000;


    public ModalActionBuilder(
            @NotNull IModalInteractionManager modalInteractionManager,
            @NotNull IModalCallback callback,
            @NotNull NamespacedKey key
    ) {

        this.modalInteractionManager = modalInteractionManager;

        this.user = callback.getUser();
        this.callback = callback;
        this.key = key;

        this.builder = LinkedTextParser.builder(modalInteractionManager.getSbds().getMessages(), user);

    }

    public @NotNull ModalCallbackAction send() {

        IModalInteractionManager.IRegisteredModalTemplate registeredModal = modalInteractionManager.getRegisteredModalTemplate(key);
        if (registeredModal == null) {
            throw new IllegalArgumentException("No modal found by key `" + key + "`");
        }

        ModalTemplate template = registeredModal.getTemplate();

        boolean isStatic = onSuccess == null;
        String id = !isStatic ? UUID.randomUUID().toString() : key.toString();

        StringParser parser = builder.build();

        Modal modal = template.build(id, parser);

        if (!isStatic) {
            modalInteractionManager.registerPendingModal(id, onSuccess, onFail, timeout);
        }

        return callback.replyModal(modal);

    }

    public void queue() {
        send().queue();
    }

    //
    // TEXTPARSER
    //

    public @NotNull LinkedTextParser.Builder parser() {
        return builder;
    }

    // PLACEHOLDERS //

    public @NotNull ModalActionBuilder withPlaceholders(@Nullable Placeholders placeholders) {
        this.builder.addPlaceholders(placeholders);
        return this;
    }

    public @NotNull ModalActionBuilder withPlaceholders(Object... placeholders) {
        this.builder.addPlaceholders(placeholders);
        return this;
    }

    // PARSERS //

    public @NotNull ModalActionBuilder withParsers(StringParser... parsers) {
        this.builder.addParsers(List.of(parsers));
        return this;
    }

    public @NotNull ModalActionBuilder withParsers(@NotNull Collection<StringParser> parsers) {
        this.builder.addParsers(parsers);
        return this;
    }

    //
    // CALLBACK
    //

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
