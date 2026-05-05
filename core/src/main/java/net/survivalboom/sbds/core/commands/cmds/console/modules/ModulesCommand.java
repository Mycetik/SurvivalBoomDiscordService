package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;

@CommandClass(name = "modules")
public class ModulesCommand extends CommandBase implements ConsoleCommand {

    public ModulesCommand() {

        addSubCommand(new ModuleInfoCommand());
        addSubCommand(new ModulesListCommand());

        addSubCommand(new EnableModuleCommand());
        addSubCommand(new DisableModuleCommand());

        addSubCommand(new LoadModuleCommand());
        addSubCommand(new UnloadModuleCommand());

    }

}
