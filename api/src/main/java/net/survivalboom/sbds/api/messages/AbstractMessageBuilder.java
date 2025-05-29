package net.survivalboom.sbds.api.messages;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractMessageBuilder<T> {

    protected final ISBDS sbds;

    protected final List<ComponentCallback<?>> callbacks = new ArrayList<>();

    protected Placeholders placeholders;

    public AbstractMessageBuilder(@NotNull ISBDS sbds) {
        this.sbds = sbds;
    }

    //
    // BUILDER
    //

    public @NotNull T withPlaceholders(@Nullable Placeholders placeholders) {
        this.placeholders = placeholders;
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

    @SuppressWarnings("unchecked")
    private @NotNull T This() {
        return (T) this;
    }


    @SuppressWarnings("unchecked")
    protected @NotNull String componentIdCreator(@NotNull Component component) {

        String id = UUID.randomUUID().toString();

        ComponentCallback<?> callback = callbacks.stream()
                .filter(c -> c.name.equals(component.name()))
                .findAny()
                .orElse(null);

        if (callback == null) {
            return id;
        }

        Consumer<?> onSuccess = callback.onSuccess;
        Runnable onFail = Objects.requireNonNullElse(callback.onFail, () -> {});
        int timeout = callback.timeout;

        switch (callback.type) {

            case BUTTON -> sbds.getButtonInteractionManager().registerPendingInteraction(id, (Consumer<ButtonInteractionInfo>) onSuccess, onFail, timeout);

            // TODO: Implement all handlers for all types of Dropdowns.

            case STRING_SELECT -> {

            }

            case USER_SELECT -> {

            }

            case ROLE_SELECT -> {

            }

            case MENTIONABLE_SELECT -> {

            }

            case CHANNEL_SELECT -> {

            }

            default -> throw new RuntimeException("Invalid component type " + callback.type);

        }

        return id;

    }


    protected record ComponentCallback<T>(
            @NotNull String name,
            @NotNull net.dv8tion.jda.api.interactions.components.Component.Type type,
            @NotNull Consumer<T> onSuccess,
            @Nullable Runnable onFail,
            int timeout
    ) {}

}
