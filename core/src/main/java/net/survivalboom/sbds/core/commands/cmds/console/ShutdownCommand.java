package net.survivalboom.sbds.core.commands.cmds.console;

import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "shutdown", aliases = {"stop", "end"}, description = "Stops SurvivalBoom Discord Service.", usage = "stop")
public class ShutdownCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        info.sbds().shutdown();
    }

}
