package net.survivalboom.sbds.core.commands.cmds.console.database.guild;

import net.survivalboom.sbds.api.commands.argument.discord.GuildArgument;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;

@CommandClass(name = "guild", description = "Manage guild data in SBDS database")
public class DatabaseGuildCommand extends CommandBase implements ConsoleCommandExecutor {

    @ArgumentMethod
    public GuildArgument guild() {
        return new GuildArgument();
    }

    @ArgumentMethod(index = 1)
    public SubCommandArgument subcommand() {
        return new SubCommandArgument(new DatabaseGuildSetCommand(), new DatabaseGuildReadCommand());
    }

}
