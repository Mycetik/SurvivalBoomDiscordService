package net.survivalboom.sbds.core.commands.cmds.console.modules;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.argument.sbds.ModuleArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.core.modules.Module;
import net.survivalboom.sbds.core.modules.ModuleManager;
import net.survivalboom.sbds.api.modules.ModuleMeta;
import net.survivalboom.sbds.core.modules.ModuleRegistration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

@CommandClass(name = "info")
public class ModuleInfoCommand extends CommandBase implements ConsoleCommand {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        Module module = ModuleManager.convertIModule(Objects.requireNonNull(info.arguments().get("module", IModule.class)));

        Logger logger = info.logger();

        logger.info("---- < Module Info > ----");
        logger.info("> Name: {}", module.getName());
        logger.info("> Description: {}", module.getMeta().getDescription());
        logger.info("> Authors: {}", String.join(", ", module.getMeta().getAuthors()));
        logger.info("> Website: {}", module.getMeta().getWebsite());
        logger.info("> Version: {}", module.getMeta().getVersion());
        logger.info(" ");
        logger.info("> Main: {}", module.getMeta().getMain());
        logger.info("> File: {}", module.getFile().getName());
        logger.info("> Dependencies: {}", String.join(", ", module.getMeta().getDependencies().stream().map(ModuleMeta.Dependency::getName).toList()));
        logger.info(" ");
        logger.info("> Status: {}", module.isEnabled() ? "Enabled" : "Disabled");
        logger.info("> Registrations: {}", String.join(", ", module.getRegistration().regList().stream().map(ModuleRegistration.Reg::name).toList()));
        logger.info("---- ---- ----- ---- ----");


    }


    @ArgumentMethod(name = "module")
    public Argument<?> module() {
        return new ModuleArgument();
    }


}
