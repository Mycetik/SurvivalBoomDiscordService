package net.survivalboom.sbds.core.console.builtin.modules;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.console.ConsoleCommand;

@Command(name = "modules")
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
