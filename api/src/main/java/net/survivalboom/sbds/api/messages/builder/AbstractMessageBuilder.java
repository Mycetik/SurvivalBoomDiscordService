package net.survivalboom.sbds.api.messages.builder;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.interaction.dropdown.entity.EntityDropdownInteractionInfo;
import net.survivalboom.sbds.api.interaction.dropdown.string.StringDropdownInteractionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractMessageBuilder<T> {

    protected final ISBDS sbds;

    protected final User user;

    protected final List<ComponentCallback<?>> callbacks = new ArrayList<>();

    private final Function<AbstractMessageBuilder<T>, MessageCreateData> messageDataSupplier;

    protected Placeholders placeholders;

    public AbstractMessageBuilder(@NotNull ISBDS sbds, @Nullable User user, @NotNull Function<AbstractMessageBuilder<T>, MessageCreateData> messageDataSupplier) {
        this.sbds = sbds;
        this.user = user;
        this.messageDataSupplier = messageDataSupplier;
    }

    //
    // BUILDER
    //

    public @NotNull T withPlaceholders(@Nullable Placeholders placeholders) {
        this.placeholders = placeholders;
        return This();
    }

    public @NotNull T withPlaceholders(@Nullable Object... args) {

        if (args == null) {
            this.placeholders = null;
            return This();
        }

        this.placeholders = Placeholders.of(args);

        return This();

    }

    //
    // BUTTONS
    //

    public @NotNull T buttonCallback(@NotNull String name, @NotNull Consumer<ButtonInteractionInfo> onSuccess, @Nullable Runnable onFail, int timeout) {
        ComponentCallback<ButtonInteractionInfo> callback = new ComponentCallback<>(name, net.dv8tion.jda.api.interactions.components.Component.Type.BUTTON, onSuccess, onFail, timeout);
        callbacks.add(callback);
        return This();
    }

    public @NotNull T buttonCallback(@NotNull String name, @NotNull Consumer<ButtonInteractionInfo> onSuccess, int timeout) {
        return buttonCallback(name, onSuccess, null, timeout);
    }

    //
    // DROPDOWNS
    //

    // ENTITY SELECT //

    public @NotNull T entityDropdownCallback(@NotNull String name, @NotNull Consumer<EntityDropdownInteractionInfo> onSuccess, @Nullable Runnable onFail, int timeout) {
        ComponentCallback<EntityDropdownInteractionInfo> callback = new ComponentCallback<>(name, net.dv8tion.jda.api.interactions.components.Component.Type.MENTIONABLE_SELECT, onSuccess, onFail, timeout);
        callbacks.add(callback);
        return This();
    }

    public @NotNull T entityDropdownCallback(@NotNull String name, @NotNull Consumer<EntityDropdownInteractionInfo> onSuccess, int timeout) {
        return entityDropdownCallback(name, onSuccess, null, timeout);
    }

    // STRING SELECT //

    public @NotNull T stringDropdownCallback(@NotNull String name, @NotNull Consumer<StringDropdownInteractionInfo> onSuccess, @Nullable Runnable onFail, int timeout) {
        ComponentCallback<StringDropdownInteractionInfo> callback = new ComponentCallback<>(name, net.dv8tion.jda.api.interactions.components.Component.Type.STRING_SELECT, onSuccess, onFail, timeout);
        callbacks.add(callback);
        return This();
    }

    public @NotNull T stringDropdownCallback(@NotNull String name, @NotNull Consumer<StringDropdownInteractionInfo> onSuccess, int timeout) {
        return stringDropdownCallback(name, onSuccess, null, timeout);
    }

    @SuppressWarnings("unchecked")
    private @NotNull T This() {
        return (T) this;
    }


    protected @NotNull MessageCreateData createMessage() {
        return messageDataSupplier.apply(this);
    }

    protected @NotNull String componentIdCreator(@NotNull Component component) {

        boolean isStatic = component.isStatic();
        String name = component.name();
        String id = isStatic && name != null ? name : UUID.randomUUID().toString();

        if (isStatic) {
            return id;
        }

        ComponentCallback<?> callback = callbacks.stream()
                .filter(c -> c.name.equals(component.name()))
                .findAny()
                .orElse(null);

        registerComponentCallback(id, callback);

        return id;

    }

    @SuppressWarnings("unchecked")
    private void registerComponentCallback(@NotNull String id, @Nullable ComponentCallback<?> callback) {

        if (callback == null) return;

        Consumer<?> onSuccess = callback.onSuccess;
        Runnable onFail = callback.onFail;
        int timeout = callback.timeout;

        switch (callback.type) {

            case BUTTON -> {
                sbds.getButtonInteractionManager().registerPendingInteraction(id, user, (Consumer<ButtonInteractionInfo>) onSuccess, onFail, timeout);
            }

            case STRING_SELECT -> {
                sbds.getStringDropdownInteractionManager().registerPendingInteraction(id, user, (Consumer<StringDropdownInteractionInfo>) onSuccess, onFail, timeout);
            }

            case USER_SELECT, ROLE_SELECT, MENTIONABLE_SELECT, CHANNEL_SELECT -> {
                sbds.getEntityDropdownInteractionManager().registerPendingInteraction(id, user, (Consumer<EntityDropdownInteractionInfo>) onSuccess, onFail, timeout);
            }

            default -> throw new RuntimeException("Invalid component type " + callback.type);

        }

    }


    protected record ComponentCallback<T>(
            @NotNull String name,
            @NotNull net.dv8tion.jda.api.interactions.components.Component.Type type,
            @NotNull Consumer<T> onSuccess,
            @Nullable Runnable onFail,
            int timeout
    ) {}

}
