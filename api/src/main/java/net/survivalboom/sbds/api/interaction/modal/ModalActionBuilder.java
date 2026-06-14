package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.api.messages.parsers.LinkedTextParser;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ModalActionBuilder {

    private final IModalInteractionManager manager;

    private final InteractionHolder interaction;

    private final @Nullable IModalInteractionManager.IRegisteredModal originModal;

    private final ModalTemplate template;


    private final LinkedTextParser.Builder parser;


    private @Nullable Consumer<ModalInteractionInfo> onSuccess;

    private @Nullable Runnable onFail;

    private int timeout = 120000;


    public ModalActionBuilder(
            @NotNull InteractionHolder interaction,
            @NotNull IModalInteractionManager.IRegisteredModal modal,
            @NotNull IModalInteractionManager manager
    ) {

        Objects.requireNonNull(interaction, "interaction == null");
        Objects.requireNonNull(modal, "modal == null");
        Objects.requireNonNull(manager, "manager == null");

        if (!(interaction.source() instanceof IModalCallback)) {
            throw new IllegalArgumentException("interaction object `" + interaction + "` is not ModalCallback");
        }

        this.originModal = modal;
        this.template = modal.getTemplate();

        this.parser = LinkedTextParser.builder(manager.getSbds().getMessages(), interaction.user());

        this.interaction = interaction;
        this.manager = manager;

    }

    public ModalActionBuilder(
            @NotNull InteractionHolder interaction,
            @NotNull ModalTemplate template,
            @NotNull IModalInteractionManager manager
    ) {

        Objects.requireNonNull(interaction, "interaction == null");
        Objects.requireNonNull(template, "template == null");
        Objects.requireNonNull(manager, "manager == null");

        if (!(interaction.source() instanceof IModalCallback)) {
            throw new IllegalArgumentException("interaction object `" + interaction + "` is not ModalCallback");
        }

        this.originModal = null;
        this.template = template;

        this.parser = LinkedTextParser.builder(manager.getSbds().getMessages(), interaction.user());

        this.interaction = interaction;
        this.manager = manager;

    }

    //
    // BUILDER
    //

    public @NotNull LinkedTextParser.Builder parser() {
        return parser;
    }

    // PLACEHOLDERS //

    public @NotNull ModalActionBuilder withPlaceholders(@Nullable Placeholders placeholders) {
        this.parser.addPlaceholders(placeholders);
        return this;
    }

    public @NotNull ModalActionBuilder withPlaceholders(Object... placeholders) {
        this.parser.addPlaceholders(placeholders);
        return this;
    }

    // PARSERS //

    public @NotNull ModalActionBuilder withParsers(StringParser... parsers) {
        this.parser.addParsers(List.of(parsers));
        return this;
    }

    public @NotNull ModalActionBuilder withParsers(@NotNull Collection<StringParser> parsers) {
        this.parser.addParsers(parsers);
        return this;
    }

    // CALLBACK //

    public @NotNull ModalActionBuilder onSuccess(@Nullable Consumer<ModalInteractionInfo> consumer) {
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



    public @Nullable Consumer<ModalInteractionInfo> getOnSuccess() {
        return onSuccess;
    }

    public @Nullable Runnable onFail() {
        return onFail;
    }

    public int getTimeout() {
        return timeout;
    }

    public @Nullable IModalInteractionManager.IRegisteredModal getOriginModal() {
        return originModal;
    }

    public @NotNull ModalTemplate getTemplate() {
        return template;
    }

    //
    // BUILD
    //

    public @NotNull ModalCallbackAction send() {

        String id = manager.createPending(this).getId();
        Modal modal = template.build(id, parser.build());

        return ((IModalCallback) interaction.source()).replyModal(modal);

    }

    public void queue() {
        send().queue();
    }


}
