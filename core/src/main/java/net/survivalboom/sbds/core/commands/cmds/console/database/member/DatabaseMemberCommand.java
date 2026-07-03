package net.survivalboom.sbds.core.commands.cmds.console.database.member;

import net.survivalboom.sbds.api.commands.argument.discord.GuildArgument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;

@CommandClass(name = "member", description = "Manager guild member data in the database")
public class DatabaseMemberCommand extends CommandBase implements ConsoleCommandExecutor {

    @ArgumentMethod
    public GuildArgument guild() {
        return new GuildArgument();
    }

    @ArgumentMethod(index = 1)
    public UserArgument user() {
        return new UserArgument();
    }

    @ArgumentMethod(index = 2)
    public SubCommandArgument subcommand() {
        return new SubCommandArgument(new DatabaseMemberSetCommand(), new DatabaseMemberReadCommand());
    }

}
