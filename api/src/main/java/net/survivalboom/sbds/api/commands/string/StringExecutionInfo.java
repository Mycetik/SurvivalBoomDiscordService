package net.survivalboom.sbds.api.commands.string;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;

public class StringExecutionInfo extends CommandExecutionInfo<IStringCommandManager.IRegisteredStringCommand, IStringCommandManager> implements InteractionHolder {

    private final Message message;

    private Message response = null;


    public StringExecutionInfo(
            @NotNull Message message,
            @NotNull IStringCommandManager.IRegisteredStringCommand rootCommand,
            @NotNull Command currentCommand,
            @NotNull String alias,
            @NotNull TypeMap arguments
    ) {
        super(rootCommand, currentCommand, alias, arguments);
        this.message = message;
    }

    @Override
    public @NotNull Object source() {
        return message;
    }

    public @NotNull Message message() {
        return message;
    }

    public Message response() {
        return response;
    }


    @Override
    public Guild guild() {
        return message.getGuild();
    }

    @Override
    public @NotNull User user() {
        return message.getAuthor();
    }

    @Override
    public Member member() {
        return message.getMember();
    }

    @Override
    public Channel channel() {
        return message.getChannel();
    }


    // EDIT //

    @Override
    public @NotNull RestAction<?> editRaw(@NotNull String txt) {

        if (response == null) {
            throw new IllegalStateException("Command has no response yet");
        }

        return response.editMessage(txt);

    }

    @Override
    public @NotNull RestAction<?> editRaw(@NotNull MessageCreateData data) {

        if (response == null) {
            throw new IllegalStateException("Command has no response yet");
        }

        return response.editMessage(MessageEditData.fromCreateData(data));

    }

    // SEND ONLY //

    @Override
    public @NotNull RestAction<?> sendRaw(@NotNull String txt) {
        return message.reply(txt).onSuccess(m -> this.response = m);
    }

    @Override
    public @NotNull RestAction<?> sendRaw(@NotNull MessageCreateData data) {
        return message.reply(data).onSuccess(m -> this.response = m);
    }

    // REPLY (INTELLIGENT) //

    @Override
    public @NotNull RestAction<?> replyRaw(@NotNull String txt) {

        if (response != null) {
            return editRaw(txt);
        }

        else {
            return sendRaw(txt);
        }

    }

    @Override
    public @NotNull RestAction<?> replyRaw(@NotNull MessageCreateData data) {

        if (response != null) {
            return editRaw(data);
        }

        else {
            return sendRaw(data);
        }

    }

}
