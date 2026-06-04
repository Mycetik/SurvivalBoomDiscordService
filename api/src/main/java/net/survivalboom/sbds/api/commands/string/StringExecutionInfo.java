package net.survivalboom.sbds.api.commands.string;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.interaction.IBasicInteractionExecution;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;

public class StringExecutionInfo extends CommandExecutionInfo<IStringCommandManager.IRegisteredStringCommand, IStringCommandManager> implements IBasicInteractionExecution {

    private final Message message;

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

    public @NotNull Message message() {
        return message;
    }


    @Override
    public @NotNull RestAction<?> editRaw(@NotNull MessageEditData data) {
        return message.editMessage(data);
    }

    @Override
    public @NotNull IReplyCallback replyCallback0() {
        return message;
    }

    @Override
    public Guild guild() {
        return null;
    }

    @Override
    public @NotNull User user() {
        return null;
    }

    @Override
    public Member member() {
        return null;
    }

    @Override
    public Channel channel() {
        return null;
    }
}
