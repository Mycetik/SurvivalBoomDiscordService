package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.argument.sbds.ModuleArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.dependencies.ModuleDependency;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@CommandClass(name = "info")
public class ModuleInfoCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        IModule module = info.arguments().getCast("module", IModule.class).orElseThrow();

        Logger logger = info.logger();

        logger.info("---- < Module Info > ----");
        logger.info("> Name: {}", module.getName());
        logger.info("> ID: {}", module.getId());
        logger.info("> Description: {}", module.getMeta().getDescription());
        logger.info("> Authors: {}", String.join(", ", module.getMeta().getAuthors()));
        logger.info("> Website: {}", module.getMeta().getWebsite());
        logger.info("> Version: {}", module.getMeta().getVersion());
        logger.info(" ");
        logger.info("> Main: {}", module.getMeta().getMain());
        logger.info("> File: {}", module.getFile());
        logger.info("> Dependencies: {}", String.join(", ", module.getMeta().getDependencies().stream().map(ModuleDependency::id).toList()));
        logger.info(" ");
        logger.info("> Status: {}", module.isEnabled() ? "Enabled" : "Disabled");
        logger.info("> Registrations: {}", String.join(", ", info.sbds().getRegistrationRegistry().getModuleRegistrations(module).stream().map(reg -> reg.regKey().toString()).toList()));
        logger.info("---- ---- ----- ---- ----");


    }


    @ArgumentMethod
    public Argument<?> module() {
        return new ModuleArgument();
    }

}
