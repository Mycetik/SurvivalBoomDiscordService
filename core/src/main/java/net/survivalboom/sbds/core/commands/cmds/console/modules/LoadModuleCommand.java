package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.argument.misc.FileArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.IModuleManager;
import net.survivalboom.sbds.api.modules.ModuleMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@CommandClass(name = "load")
public class LoadModuleCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        File file = info.arguments().getCast("file", File.class).orElseThrow();
        IModuleManager manager = info.sbds().getModuleManager();

        IModuleManager.ModuleMetaLoadResult result;
        try {
            result = manager.loadModuleMeta(file);
        }

        catch (ModuleMeta.InvalidMetaException e) {
            info.logger().error("Invalid module file `{}`.", file.getName(), e);
            return;
        }

        String name = result.meta().getName();

        IModule module;
        try {
            module = info.sbds().getModuleManager().createModule(result);
        }

        catch (IModuleManager.ModuleUnsatisfiedDependencyException e) {
            info.logger().error("Module `{}` requires `{}` as dependency. No module with that id was found.", name, e.getDependency().id());
            return;
        }

        catch (IModuleManager.ModuleUnsatisfiedLibraryException e) {
            info.logger().error("Failed to download library `{}`. Refusing to load.", e.getLibrary(), e.getCause());
            return;
        }

        catch (IModuleManager.ModuleRefusedException e) {
            info.logger().error("Module `{}` refused to load. Maybe it hates you?", name, e);
            return;
        }

        catch (IModuleManager.ModuleLoadingException e) {
            info.logger().error("Failed to load module `{}`. {}", name, e.getMessage(), e.getCause());
            return;
        }

        info.logger().info("Successfully loaded module `{}`.", module.getName());

    }

    @ArgumentMethod
    public Argument<?> file(ISBDS sbds) {
        return new FileArgument(sbds.getModuleManager().getModulesDir(), true);
    }

}
