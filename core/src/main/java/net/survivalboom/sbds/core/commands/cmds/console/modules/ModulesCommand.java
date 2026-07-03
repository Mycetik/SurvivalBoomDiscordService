package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;

@CommandClass(name = "modules", description = "Manage SBDS modules")
public class ModulesCommand extends CommandBase implements ConsoleCommandExecutor {

    @ArgumentMethod
    public SubCommandArgument subcommand() {
        return new SubCommandArgument(
                new ModuleInfoCommand(),
                new ModulesListCommand(),
                new EnableModuleCommand(),
                new DisableModuleCommand(),
                new RestartModuleCommand(),
                new ReloadModuleCommand(),
                new LoadModuleCommand(),
                new UnloadModuleCommand()
        );
    }

}
