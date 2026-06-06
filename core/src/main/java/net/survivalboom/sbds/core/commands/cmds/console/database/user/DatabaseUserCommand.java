package net.survivalboom.sbds.core.commands.cmds.console.database.user;

import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;

@CommandClass(name = "user", description = "Manage user data in SBDS database")
public class DatabaseUserCommand extends CommandBase implements ConsoleCommandExecutor {

    @ArgumentMethod
    public UserArgument user() {
        return new UserArgument();
    }

    @ArgumentMethod(index = 1)
    public SubCommandArgument subcommand() {
        return new SubCommandArgument(new DatabaseUserSetCommand(), new DatabaseUserReadCommand());
    }


}
