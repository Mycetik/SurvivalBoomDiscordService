package net.survivalboom.sbds.api.commands.string;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.messages.MessageActionBuilder;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StringExecutionInfo extends CommandExecutionInfo {

    private final Message message;

    public StringExecutionInfo(@NotNull Command command, @NotNull Message message, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {
        super(command, alias, arguments, logger, sbds);
        this.message = message;
    }

    public @NotNull Message message() {
        return message;
    }

    public @Nullable Guild guild() {
        return this.message.getGuild();
    }

    public @Nullable Member guildMember() {
        return this.message.getMember();
    }

    public @NotNull User user() {
        return this.message.getAuthor();
    }

    public @NotNull MessageActionBuilder<MessageCreateAction> reply(@NotNull String name) {
        return messages().createActionMessage(name, user(), d -> message().reply(d));
    }

}
