package net.survivalboom.sbds.core.commands.cmds.console;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import org.jetbrains.annotations.NotNull;

@Command(name = "shutdown", aliases = {"stop", "end"}, description = "Stops SurvivalBoom Discord Service.", usage = "stop")
public class ShutdownCommand extends CommandBase implements ConsoleCommand {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        info.sbds().shutdown();
    }

}
