package net.survivalboom.sbds.core.commands.builtin.console.modules;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.console.ConsoleCommand;
import net.survivalboom.sbds.api.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Command(name = "list")
public class ModulesListCommand extends CommandBase implements ConsoleCommand {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        List<IModule> modules = info.sbds().getModuleManager().getModules();

        if (modules.isEmpty()) {
            info.logger().info("Nothing to show. No modules loaded.");
            return;
        }

        info.logger().info("---- < Loaded Modules List > ----");

        for (IModule module : modules) {
            info.logger().info("> {} v{} ~ {} - {}", module.getName(), module.getMeta().getVersion(), module.isEnabled() ? "Enabled" : "Disabled", module.getMeta().getDescription());
        }

        info.logger().info("* Use `modules info <Module>` to view module details.");
        info.logger().info("---- ---- ---- --- ---- ---- ----");

    }

}
