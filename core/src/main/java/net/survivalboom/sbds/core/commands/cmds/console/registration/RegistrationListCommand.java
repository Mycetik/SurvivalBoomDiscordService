package net.survivalboom.sbds.core.commands.cmds.console.registration;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@CommandClass(name = "list", description = "Show all SBDS registrations")
public class RegistrationListCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        info.logger().info("--- --- < Registrations List > --- ---");
        info.logger().info(" ");

        List<Registration<?>> regs = info.sbds().getRegistrationRegistry().getRegistrations();
        Map<IModule, List<Registration<?>>> regsSorted = new HashMap<>();

        for (Registration<?> reg : regs) {
            regsSorted.computeIfAbsent(reg.module(), k -> new ArrayList<>()).add(reg);
        }

        for (var entry : regsSorted.entrySet()) {

            info.logger().info("> {}", Objects.requireNonNullElse(entry.getKey(), "SBDS"));
            for (Registration<?> reg : entry.getValue()) {
                info.logger().info("* {} ({}) -> {}", reg.regKey(), reg.key(), reg.object());
            }

            info.logger().info(" ");

        }

        info.logger().info("--- --- ---- --- ---- --- ---- --- ---");

    }

}
