package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.argument.sbds.ModuleArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "unload")
public class UnloadModuleCommand extends CommandBase implements ConsoleCommand {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        IModule module = info.arguments().get("module", IModule.class);
        assert module != null;

        info.sbds().getModuleManager().unloadModule(module);

        info.logger().info("Successfully unloaded module `{}`.", module.getName());

    }

    @ArgumentMethod(name = "module")
    public Argument<?> module() {
        return new ModuleArgument(null);
    }

}
