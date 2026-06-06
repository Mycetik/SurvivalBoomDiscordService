package net.survivalboom.sbds.core.commands.cmds.console.database;

import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.core.commands.cmds.console.database.guild.DatabaseGuildCommand;
import net.survivalboom.sbds.core.commands.cmds.console.database.user.DatabaseUserCommand;

@CommandClass(name = "database", description = "Manage SBDS database")
public class DatabaseCommand extends CommandBase implements ConsoleCommandExecutor {

    @ArgumentMethod
    public SubCommandArgument subcommand() {
        return new SubCommandArgument(new DatabaseGuildCommand(), new DatabaseUserCommand());
    }

}
