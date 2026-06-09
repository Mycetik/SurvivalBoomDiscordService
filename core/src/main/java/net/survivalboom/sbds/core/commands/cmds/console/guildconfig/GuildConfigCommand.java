package net.survivalboom.sbds.core.commands.cmds.console.guildconfig;

import net.survivalboom.sbds.api.commands.argument.discord.GuildArgument;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;

@CommandClass(name = "gconfig", description = "Manage SBDS per guild configuration")
public class GuildConfigCommand extends CommandBase implements ConsoleCommandExecutor {

    @ArgumentMethod
    public GuildArgument guild() {
        return new GuildArgument();
    }

    @ArgumentMethod
    public SubCommandArgument subcommand() {
        return new SubCommandArgument(new GuildConfigReadCommand(), new GuildConfigSetCommand());
    }

}
