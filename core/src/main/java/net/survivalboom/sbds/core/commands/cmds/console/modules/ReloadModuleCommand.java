package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.argument.sbds.ModuleArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleLoadingException;
import net.survivalboom.sbds.api.modules.ModuleRefusedException;
import net.survivalboom.sbds.api.modules.ModuleStateCallbackException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@CommandClass(name = "reload")
public class ReloadModuleCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        IModule module = info.arguments().getCast("module", IModule.class).orElseThrow();
        String moduleName = module.getName();

        boolean wasEnabled = module.isEnabled();
        File rawModuleFile = module.getFile().file();

        if (wasEnabled) {
            try {
                info.sbds().getModuleManager().disableModule(module);
            } catch (ModuleStateCallbackException e) {
                info.logger().error("Failed to disable module `{}` properly during reload!", moduleName, e);
                return;
            }
        }

        try {
            info.sbds().getModuleManager().unloadModule(module);
        } catch (ModuleStateCallbackException e) {
            info.logger().error("Failed to unload module `{}` properly during reload!", moduleName, e);
            return;
        }

        IModule reloadedModule;
        try {
            reloadedModule = info.sbds().getModuleManager().loadModule(rawModuleFile);
        } catch (ModuleLoadingException | ModuleRefusedException e) {
            info.logger().error("Module file `{}` failed to load during reload!", rawModuleFile.getName(), e);
            return;
        }

        if (wasEnabled) {
            try {
                info.sbds().getModuleManager().enableModule(reloadedModule);
            } catch (ModuleStateCallbackException | ModuleRefusedException e) {
                info.logger().error("Failed to enable module `{}` after reloading!", reloadedModule.getName(), e);
                return;
            }
        }

        info.logger().info("Successfully reloaded module `{}`.", reloadedModule.getName());
    }

    @ArgumentMethod
    public Argument<?> module() {
        return new ModuleArgument();
    }

}