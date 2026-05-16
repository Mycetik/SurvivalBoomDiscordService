package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.argument.sbds.ModuleArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleRefusedException;
import net.survivalboom.sbds.api.modules.ModuleStateCallbackException;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "enable")
public class EnableModuleCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        IModule module = info.arguments().getCast("module", IModule.class).orElseThrow();

        try {
            info.sbds().getModuleManager().enableModule(module);
        }

        catch (ModuleStateCallbackException | ModuleRefusedException e) {
            info.logger().error("Failed to enable module `{}`. An exception was thrown.", module.getName(), e);
        }

        info.logger().info("Successfully enabled module `{}`.", module.getName());

    }


    @ArgumentMethod
    public Argument<?> module() {
        return new ModuleArgument(false);
    }

}
