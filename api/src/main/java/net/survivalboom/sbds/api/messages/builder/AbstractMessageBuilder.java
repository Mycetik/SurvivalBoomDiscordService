package net.survivalboom.sbds.api.messages.builder;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.survivalboom.sbds.api.interaction.component.ComponentInteractionRequest;
import net.survivalboom.sbds.api.interaction.component.IComponentInteractionManager;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.MessageInteractableComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.LinkedTextParser;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractMessageBuilder<it extends AbstractMessageBuilder<it>> implements ComponentLinker {

    protected final LinkedTextParser.Builder builder;

    protected ComponentInteractionRequest components = null;

    protected Map<String, String> generatedComponentsIds = null;

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
    // COMPONENTS
    //

    public @NotNull it setComponents(@Nullable ComponentInteractionRequest request) {
        this.components = request;
        return it();
    }

    public @NotNull it withComponents(@NotNull Consumer<ComponentInteractionRequest.Builder> builder) {
        ComponentInteractionRequest.Builder b = ComponentInteractionRequest.builder();
        builder.accept(b);
        b.setTarget(this.builder.getTarget());
        return setComponents(b.build());
    }

    //
    // BUILD
    //

    public @NotNull MessageCreateBuilder build() {

        LinkedTextParser parser = builder.build();

        IMessages messages = parser.getMessages();

        IMessageTemplate template = builder.getMessages().getMessage(messageKey, builder.getTarget(), true);
        if (template == null) {
            return new MessageCreateBuilder().setContent(messageKey);
        }

        if (components != null) {
            IComponentInteractionManager manager = messages.getSbds().getComponentInteractionManager();
            IComponentInteractionManager.IPendingInteraction pending = manager.createPending(components);
            generatedComponentsIds = pending.getGeneratedIds();
        }

        else {
            generatedComponentsIds = null;
        }

        return template.createMessageData(parser, this);

    }

    @Override
    public @NotNull String link(@NotNull MessageInteractableComponentTemplate<?> component) {

        if (generatedComponentsIds == null) {
            throw new IllegalStateException("Component ids were not generated yet!");
        }

        String componentName = component.getName();
        String id = generatedComponentsIds.get(componentName);
        if (id == null) {
            throw new IllegalArgumentException("There is no generated id for component with name `" + componentName + "`. Generated: " + generatedComponentsIds);
        }

        return id;

    }


    @SuppressWarnings("unchecked")
    private @NotNull it it() {
        return (it) this;
    }

}
