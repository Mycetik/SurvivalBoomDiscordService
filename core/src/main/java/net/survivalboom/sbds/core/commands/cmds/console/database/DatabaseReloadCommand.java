package net.survivalboom.sbds.core.commands.cmds.console.database;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.core.database.Database;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "reload", description = "Reloads the database")
public class DatabaseReloadCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        Database database = (Database) info.sbds().getDatabase();
        database.reload0(null, false, true, true, null);

    }

}
