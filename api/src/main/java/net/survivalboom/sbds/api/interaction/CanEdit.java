package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import org.jetbrains.annotations.NotNull;

public interface CanEdit extends InteractionHolder {

    @NotNull RestAction<?> editRaw(@NotNull MessageEditData data);


    default @NotNull MessageActionBuilder<RestAction<?>> edit(@NotNull String key) {
        return new MessageActionBuilder<>(messages(), user(), key, d -> editRaw(MessageEditData.fromCreateData(d)));
    }

    default @NotNull RestAction<?> editRaw(@NotNull String text) {
        return editRaw(MessageEditData.fromContent(text));
    }

}
