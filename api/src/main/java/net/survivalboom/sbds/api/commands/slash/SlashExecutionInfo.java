package net.survivalboom.sbds.api.commands.slash;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.interaction.modal.ModalActionBuilder;
import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.messages.MessageActionBuilder;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;

public class SlashExecutionInfo extends CommandExecutionInfo {

    protected final SlashCommandInteraction interaction;

    public SlashExecutionInfo(@NotNull Command command, @NotNull SlashCommandInteraction interaction, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {
        super(command, alias, arguments, logger, sbds);
        this.interaction = interaction;
    }

    public @NotNull SlashCommandInteraction interaction() {
        return interaction;
    }

    public @Nullable Guild guild() {
        return this.interaction.getGuild();
    }

    public @Nullable Member guildMember() {
        return this.interaction.getMember();
    }

    public @NotNull User user() {
        return this.interaction.getUser();
    }


    public @NotNull ReplyCallbackAction replyRaw(@NotNull String text) {
        return interaction.reply(text);
    }

    public @NotNull MessageActionBuilder<ReplyCallbackAction> reply(@NotNull String key) {
        return MessageActionBuilder.create(messages(), key, user(), interaction::reply);
    }

    public @NotNull WebhookMessageEditAction<Message> editRaw(@NotNull String text) {
        return interaction.getHook().editOriginal(MessageEditData.fromContent(text));
    }

    public @NotNull MessageActionBuilder<WebhookMessageEditAction<Message>> edit(@NotNull String key) {
        return MessageActionBuilder.create(messages(), key, user(), d -> interaction.getHook().editOriginal(MessageEditData.fromCreateData(d)));
    }

    public @NotNull ModalActionBuilder replyModal(@NotNull String key) {
        return new ModalActionBuilder(sbds.getModalInteractionManager(), interaction, NamespacedKey.fromString(key));
    }

    protected @NotNull IMessage getMessage(@NotNull String key) {
        return Objects.requireNonNull(messages().getMessage(key, user(), true));
    }

}
