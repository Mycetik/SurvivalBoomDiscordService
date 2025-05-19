package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMessages {

    @Nullable IMessage getMessage(@NotNull String name, @NotNull IUserData userData, boolean fallback);

    @Nullable IMessage getMessage(@NotNull String name, @NotNull User user, boolean fallback);


    @Nullable MessageCreateData getMessageData(@NotNull String name, @Nullable Placeholders placeholders, @Nullable IUserData userData, boolean fallback);

    @Nullable MessageCreateData getMessageData(@NotNull String name,  @Nullable Placeholders placeholders, @Nullable User user, boolean fallback);


    @NotNull MessageCreateData getMessageDataFallback(@NotNull String name, @Nullable Placeholders placeholders, @Nullable IUserData userData);

    @NotNull MessageCreateData getMessageDataFallback(@NotNull String name, @Nullable Placeholders placeholders, @Nullable User user);


    @NotNull ReplyCallbackAction reply(@NotNull IReplyCallback interaction, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user);

    @NotNull ReplyCallbackAction reply(@NotNull IReplyCallback interaction, @Nullable Placeholders placeholders, @NotNull String name, @Nullable Member member);

    @NotNull ReplyCallbackAction reply(@NotNull IReplyCallback interaction, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData userData);

    @NotNull MessageCreateAction reply(@NotNull Message message, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user);

    @NotNull MessageCreateAction reply(@NotNull Message message, @Nullable Placeholders placeholders, @NotNull String name, @Nullable Member member);

    @NotNull MessageCreateAction reply(@NotNull Message message, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData userData);


    @NotNull MessageEditAction edit(@NotNull Message message, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user);

    @NotNull MessageEditAction edit(@NotNull Message message, @Nullable Placeholders placeholders, @NotNull String name, @Nullable Member member);

    @NotNull MessageEditAction edit(@NotNull Message message, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData user);

    @NotNull WebhookMessageEditAction<Message> edit(@NotNull InteractionHook interaction, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user);

    @NotNull WebhookMessageEditAction<Message> edit(@NotNull InteractionHook interaction, @Nullable Placeholders placeholders, @NotNull String name, @Nullable Member member);

    @NotNull WebhookMessageEditAction<Message> edit(@NotNull InteractionHook interaction, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData user);


    @NotNull MessageCreateAction sendMessage(@NotNull TextChannel channel, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user);

    @NotNull MessageCreateAction sendMessage(@NotNull TextChannel channel, @Nullable Placeholders placeholders, @NotNull String name, @Nullable Member member);

    @NotNull MessageCreateAction sendMessage(@NotNull TextChannel channel, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData userData);


    @NotNull MessageCreateAction sendMessage(@NotNull VoiceChannel channel, @Nullable Placeholders placeholders, @NotNull String name, @Nullable User user);

    @NotNull MessageCreateAction sendMessage(@NotNull VoiceChannel channel, @Nullable Placeholders placeholders, @NotNull String name, @Nullable IUserData userData);

}
