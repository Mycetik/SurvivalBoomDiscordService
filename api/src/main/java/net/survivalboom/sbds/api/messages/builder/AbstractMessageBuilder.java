package net.survivalboom.sbds.api.messages.builder;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.interaction.ComponentInteractionInfo;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.MessageInteractableComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.LinkedTextParser;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.messages.template.TextMessageTemplate;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractMessageBuilder<it extends AbstractMessageBuilder<it>> implements ComponentLinker {

    protected final LinkedTextParser.Builder builder;

    protected final List<ComponentCallback<?>> callbacks = new ArrayList<>();

    protected final String messageKey;

    public AbstractMessageBuilder(
            @NotNull IMessages messages,
            @NotNull User user,
            @NotNull String messageKey
    ) {

        Objects.requireNonNull(messageKey, "messageKey == null");

        this.builder = LinkedTextParser.builder(messages, user);
        this.messageKey = messageKey;

    }

    //
    // TEXT PARSER
    //

    // PLACEHOLDERS //

    public @NotNull it withPlaceholder(@NotNull String key, @Nullable Object value) {
        this.builder.addPlaceholder(key, value);
        return it();
    }

    public @NotNull it withPlaceholders(@Nullable Placeholders placeholders) {
        this.builder.addPlaceholders(placeholders);
        return it();
    }

    public @NotNull it withPlaceholders(Object... args) {
        this.builder.addPlaceholders(args);
        return it();
    }

    // PARSERS //

    public @NotNull it withParser(@NotNull StringParser parser) {
        this.builder.addParser(parser);
        return it();
    }

    public @NotNull it withParser(@NotNull Collection<StringParser> parsers) {
        this.builder.addParsers(parsers);
        return it();
    }

    //
    // CALLBACKS
    //

    // BUTTONS //

    public @NotNull it buttonCallback(
            @NotNull String name,
            boolean userSpecific,
            @NotNull Consumer<ComponentInteractionInfo<ButtonInteractionEvent>> onSuccess,
            @Nullable Runnable onFail,
            int timeout
    ) {
        var callback = new ComponentCallback<>(name, userSpecific, ButtonInteractionEvent.class, onSuccess, onFail, timeout);
        callbacks.add(callback);
        return it();
    }

    // ENTITY SELECT //

    public @NotNull it entityDropdownCallback(
            @NotNull String name,
            boolean userSpecific,
            @NotNull Consumer<ComponentInteractionInfo<EntitySelectInteractionEvent>> onSuccess,
            @Nullable Runnable onFail,
            int timeout
    ) {
        var callback = new ComponentCallback<>(name, userSpecific, EntitySelectInteractionEvent.class, onSuccess, onFail, timeout);
        callbacks.add(callback);
        return it();
    }

    // STRING SELECT //

    public @NotNull it stringDropdownCallback(
            @NotNull String name,
            boolean userSpecific,
            @NotNull Consumer<ComponentInteractionInfo<StringSelectInteractionEvent>> onSuccess,
            @Nullable Runnable onFail,
            int timeout
    ) {
        var callback = new ComponentCallback<>(name, userSpecific, StringSelectInteractionEvent.class, onSuccess, onFail, timeout);
        callbacks.add(callback);
        return it();
    }

    //
    // BUILD
    //

    public @NotNull MessageCreateData build() {

        StringParser parser = builder.build();

        IMessageTemplate template = builder.getMessages().getMessage(messageKey, builder.getTarget(), true);
        if (template == null) {
            return MessageCreateData.fromContent(messageKey);
        }

        return template.createMessageData(parser, this);

    }

    @Override
    public @NotNull String link(@NotNull MessageInteractableComponentTemplate<?> component) {

        String id = UUID.randomUUID().toString();
        if (component.isStatic()) {
            throw new RuntimeException("tried to link static component");
        }

        callbacks.stream()
                .filter(c -> c.name.equals(component.getName()))
                .findAny()
                .ifPresent(callback -> registerComponentCallback(id, callback));

        return id;

    }

    private <T extends GenericComponentInteractionCreateEvent> void registerComponentCallback(@NotNull String id, @NotNull ComponentCallback<T> callback) {

        boolean userSpecific = callback.userSpecific;
        Class<T> clazz = callback.clazz;

        Consumer<ComponentInteractionInfo<T>> onSuccess = callback.onSuccess;
        Runnable onFail = callback.onFail;

        int timeout = callback.timeout;

        builder.getMessages().getSbds().getComponentInteractionManager().registerPendingInteraction(
                id,
                userSpecific ? builder.getTarget() : null,
                clazz,
                onSuccess,
                onFail,
                timeout
        );

    }


    @SuppressWarnings("unchecked")
    private @NotNull it it() {
        return (it) this;
    }


    protected record ComponentCallback<event extends GenericComponentInteractionCreateEvent>(

            @NotNull String name,

            boolean userSpecific,
            Class<event> clazz,

            @NotNull Consumer<ComponentInteractionInfo<event>> onSuccess,
            @Nullable Runnable onFail,

            int timeout

    ) {}

}
