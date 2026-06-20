package net.survivalboom.sbds.core.commands.cmds.console.database;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.database.IRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CommandClass(name = "repositories", aliases = {"repos"}, description = "Show a list of current registered repositories in the database")
public class DatabaseRepositoriesCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        IDatabase database = info.sbds().getDatabase();
        List<IRepository<?>> repositories = database.getRepositories();

        info.logger().info("--- --- < Repositories List > --- ---");

        for (IRepository<?> repository : repositories) {
            info.logger().info("* {} -> {}", repository.getRegistration().key(), repository.getRecordClass().getSimpleName() + ".class");
        }

        info.logger().info("--- --- --- ---- ---- --- --- --- ---");

    }
}
