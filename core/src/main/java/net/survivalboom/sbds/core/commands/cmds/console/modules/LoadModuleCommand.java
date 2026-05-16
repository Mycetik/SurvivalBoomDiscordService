package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.argument.misc.FileArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleLoadingException;
import net.survivalboom.sbds.api.modules.ModuleRefusedException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@CommandClass(name = "load")
public class LoadModuleCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        File file = info.arguments().getCast("file", File.class).orElseThrow();

        IModule module;
        try {
            module = info.sbds().getModuleManager().loadModule(file);
        }

        catch (ModuleLoadingException | ModuleRefusedException e) {
            info.logger().error("Module file `{}` failed to load!", file.getName(), e);
            return;
        }

        info.logger().info("Successfully loaded module `{}`.", module.getName());

    }

    @ArgumentMethod
    public Argument<?> file() {
        return new FileArgument(sbds -> sbds.getModuleManager().getModulesDir(), true);
    }

}
