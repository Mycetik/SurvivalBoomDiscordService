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

@CommandClass(name = "remove", description = "Remove a registration")
public class RegistrationRemoveCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        NamespacedKey key = info.arguments().getCast("key", NamespacedKey.class).orElseThrow();

        Registration<?> reg = info.sbds().getRegistrationRegistry().getRegistration(key);
        if (reg == null) {
            info.logger().error("Unknown registration `{}`.", key);
            return;
        }

        info.sbds().getRegistrationRegistry().removeRegistration(reg);

        info.logger().info("Unregistered `{}` successfully.", key);

    }

    @ArgumentMethod
    public NamespacedKeyArgument key() {
        return new NamespacedKeyArgument();
    }

}
