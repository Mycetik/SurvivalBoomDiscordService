package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.argument.sbds.ModuleArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.IModuleManager;
import net.survivalboom.sbds.api.modules.ModuleFile;
import net.survivalboom.sbds.api.modules.ModuleMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@CommandClass(name = "reload")
public class ReloadModuleCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        IModule module = info.arguments().getCast("module", IModule.class).orElseThrow();
        String name = module.getName();


        ModuleFile moduleFile = module.getFile();
        if (moduleFile == null) {
            info.logger().error("Module was not loaded from a file. Module file is null.");
            return;
        }


        File file = module.getFile().file();
        if (!file.exists() || !file.isFile()) {
            info.logger().error("Module file `{}` no longer exists.", file.getName());
            return;
        }

        boolean wasEnabled = module.isEnabled();
        IModuleManager manager = info.sbds().getModuleManager();

        //
        // ВИМИКАЄМО МОДУЛЬ
        //

        if (wasEnabled) {
            try {
                manager.disableModule(module);
            } catch (IModuleManager.ModuleStateCallbackException e) {
                info.logger().error("Failed to disable module `{}` properly. This may cause memory leaks and undefined behaviour.", name, e);
                return;
            }
        }

        //
        // ВІДВАНТАЖУЄМО МОДУЛЬ
        //

        try {
            manager.unloadModule(module);
        }

        catch (IModuleManager.ModuleDependantException e) {
            info.logger().error(e.getMessage());
            return;
        }

        catch (IModuleManager.ModuleStateCallbackException e) {
            info.logger().error("Failed to unload module `{}` properly. This may cause memory leaks and undefined behaviour.", name, e);
            return;
        }

        //
        // ЗАВАНТАЖУЄМО МОДУЛЬ
        //

        // Завантажуємо ModuleMeta та перевіряємо його на правильність //

        IModuleManager.ModuleMetaLoadResult result;
        try {
            result = manager.loadModuleMeta(file);
        }

        catch (ModuleMeta.InvalidMetaException e) {
            info.logger().error("Invalid module file `{}`.", file.getName(), e);
            return;
        }

        String newName = result.meta().getName();
        if (!newName.equals(name)) {
            info.logger().error("Module from file `{}` has a different name ({}). Refusing to load.", file.getName(), newName);
            return;
        }

        // ЗАВАНТАЖУЄМО МОДУЛЬ //

        IModule reloadedModule;
        try {
            reloadedModule = manager.createModule(result);
        }

        catch (IModuleManager.ModuleUnsatisfiedDependencyException e) {
            info.logger().error("Module `{}` requires `{}` as a dependency. No module with that id was found.", name, e.getDependency().id());
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

        //
        // ВМИКАЄМО МОДУЛЬ
        //

        if (wasEnabled) {

            try {
                manager.enableModule(reloadedModule);
            }

            catch (IModuleManager.ModuleRefusedException e) {
                info.logger().error("Module `{}` refused to start. Maybe it hates you?", name, e);
            }

            catch (IModuleManager.ModuleStateCallbackException e) {
                info.logger().error("An exception was thrown when attempted to enable module `{}`.", name, e);
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