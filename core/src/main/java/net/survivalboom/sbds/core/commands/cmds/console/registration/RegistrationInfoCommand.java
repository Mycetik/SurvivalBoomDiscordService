package net.survivalboom.sbds.core.commands.cmds.console.registration;

import net.survivalboom.sbds.api.commands.argument.sbds.NamespacedKeyArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@CommandClass(name = "info", description = "Show registration information")
public class RegistrationInfoCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        NamespacedKey key = info.arguments().getCast("key", NamespacedKey.class).orElseThrow();

        Registration<?> reg = info.sbds().getRegistrationRegistry().getRegistration(key);
        if (reg == null) {
            info.logger().error("Unknown registration `{}`.", key);
            return;
        }

        info.logger().info("--- --- < Registration Info > --- ---");
        info.logger().info("> Module: {}", Objects.requireNonNullElse(reg.module(), "SBDS"));
        info.logger().info("> Key: {}", reg.key());
        info.logger().info("> Global key: {}", reg.regKey());
        info.logger().info("> Object: {}", reg.object());
        info.logger().info("> Unregister action: {}", reg.unregisterAction());
        info.logger().info("--- --- ---- --- --- --- --- ---- ---");


    }

    @ArgumentMethod
    public NamespacedKeyArgument key() {
        return new NamespacedKeyArgument();
    }

}
