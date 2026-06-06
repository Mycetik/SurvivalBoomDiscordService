package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * <bold>Увага! Від цього класу смердить помиями! Триматись подалі задля вашого ж здорового глузду!</bold>
 * Минулий розробник, що намагався реалізувати нормальну систему раптово зник. Довелось використати "брутфорс" аби не повторити його долю.
 */
public interface InteractionHolder {

    @NotNull ISBDS sbds();

    @NotNull IMessages messages();


    @NotNull Object source();


    Guild guild();

    @NotNull User user();

    Member member();

    Channel channel();

    //
    // FUNCTIONS
    //

    default boolean hasPermission(@NotNull String permission) {
        return sbds().getPermissionManager().hasPermission(member(), permission, false);
    }

    // EDIT //

    default @NotNull RestAction<?> editRaw(@NotNull String txt) {
        throw new IllegalStateException("No edit method applicable to `" + this + "`");
    }

    default @NotNull RestAction<?> editRaw(@NotNull MessageCreateData data) {
        throw new IllegalStateException("No edit method applicable to `" + this + "`");
    }

    default @NotNull MessageActionBuilder edit(@NotNull String key) {
        return new MessageActionBuilder(messages(), user(), key, this::editRaw);
    }

    // SEND ONLY //

    default @NotNull RestAction<?> sendRaw(@NotNull String txt) {
        throw new IllegalStateException("No send method applicable to `" + this + "`");
    }

    default @NotNull RestAction<?> sendRaw(@NotNull MessageCreateData data) {
        throw new IllegalStateException("No send method applicable to `" + this + "`");
    }

    default @NotNull MessageActionBuilder send(@NotNull String key) {
        return new MessageActionBuilder(messages(), user(), key, this::sendRaw);
    }

    // REPLY (INTELLIGENT) //

    default @NotNull RestAction<?> replyRaw(@NotNull String txt) {
        throw new IllegalStateException("No reply method applicable to `" + this + "`");
    }

    default @NotNull RestAction<?> replyRaw(@NotNull MessageCreateData data) {
        throw new IllegalStateException("No reply method applicable to `" + this + "`");
    }

    default @NotNull MessageActionBuilder reply(@NotNull String key) {
        return new MessageActionBuilder(messages(), user(), key, this::replyRaw);
    }

    // MODAL //

    default @NotNull ModalActionBuilder replyModal(@NotNull String key) {

        if (!(source() instanceof IModalCallback callback)) {
            throw new IllegalStateException("Execution `" + this + "` cannot send a modal");
        }

        return new ModalActionBuilder(sbds().getModalInteractionManager(), callback, NamespacedKey.fromString(key));

    }


}
