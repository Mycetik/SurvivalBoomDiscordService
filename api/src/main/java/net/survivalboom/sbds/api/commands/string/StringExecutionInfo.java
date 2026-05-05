package net.survivalboom.sbds.api.commands.string;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.messages.builder.MessageActionBuilder;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringExecutionInfo extends CommandExecutionInfo<IStringCommandManager.IRegisteredStringCommand, IStringCommandManager> {

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

    public @Nullable Guild guild() {
        return this.message.getGuild();
    }

    public @Nullable Member member() {
        return this.message.getMember();
    }

    public @NotNull User user() {
        return this.message.getAuthor();
    }

    public @NotNull MessageActionBuilder<MessageCreateAction> reply(@NotNull String name) {
        return messages().createActionMessage(name, user(), d -> message().reply(d));
    }

}
