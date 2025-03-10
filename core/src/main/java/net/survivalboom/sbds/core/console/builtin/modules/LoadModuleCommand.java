package net.survivalboom.sbds.core.console.builtin.modules;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.misc.FileArgument;
import net.survivalboom.sbds.api.console.ConsoleCommand;
import net.survivalboom.sbds.api.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Command(name = "load")
public class LoadModuleCommand extends CommandBase implements ConsoleCommand {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        File file = info.arguments().get("file", File.class);
        assert file != null;

        IModule module = info.sbds().getModuleManager().loadModule(file);
        if (module == null) {
            info.logger().error("Failed to load module file `{}`. Check errors above.", file.getName());
            return;
        }

        info.sbds().getModuleManager().enableModule(module);
        if (!module.isEnabled()) {
            info.logger().warn("Module successfully enabled, but an error occurred while attempting to enable module. Check errors above.");
            return;
        }

        info.logger().info("Successfully loaded and enabled module `{}`.", module.getName());

    }


    @CommandArgument(name = "file")
    public Argument<?> file() {
        return new FileArgument(sbds -> sbds.getModuleManager().getModulesDir(), true);
    }

}
