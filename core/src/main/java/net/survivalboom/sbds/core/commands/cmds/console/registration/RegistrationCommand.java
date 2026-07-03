package net.survivalboom.sbds.core.commands.cmds.console.registration;

import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;

@CommandClass(name = "registration", aliases = "reg", description = "Manage SBDS registrations")
public class RegistrationCommand extends CommandBase implements ConsoleCommandExecutor {

    @ArgumentMethod
    public SubCommandArgument subcommand() {
        return new SubCommandArgument(
                new RegistrationListCommand(),
                new RegistrationInfoCommand(),
                new RegistrationRemoveCommand()
        );
    }

}
