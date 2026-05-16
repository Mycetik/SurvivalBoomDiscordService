package net.survivalboom.sbds.core.commands.cmds.console;

import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.console.IConsoleListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

@CommandClass(name = "help", description = "Shows a list of available commands")
public class HelpCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        Logger logger = info.logger();

        Map<String, List<IConsoleListener.IRegisteredConsoleCommand>> registeredCommandMap = info.sbds().getConsoleListener().getCommands()
                .stream()
                .collect(Collectors.groupingBy(c -> c.getRegistration().key().prefix()));

        logger.info(" ");
        logger.info("---- < Available Commands List > ----");

        registeredCommandMap.forEach((registrar, commands) -> {
            logger.info("{}:", registrar);
            commands.forEach(command -> logger.info(formatCommand(command.getCommand())));
        });

        logger.info(" ");
        logger.info("* <> - Required; [] - Optional;");
        logger.info("---- ---- ---- ------- ---- ---- ----");

    }

    private String formatCommand(net.survivalboom.sbds.api.commands.Command command) {
        String usage = genUsage(command);
        String description = Objects.requireNonNullElse(command.getDescription(), "Command has no description.");
        return String.format("> %s - %s", usage, description);
    }

    private @NotNull String genUsage(@NotNull net.survivalboom.sbds.api.commands.Command command) {

//        String usage = command.getUusage();
//        if (usage != null) {
//            return usage;
//        }

//        if (command.hasSubcommands()) {
//            return command.getName() + " " + "<" + String.join("/", command.subcommands().stream().map(net.survivalboom.sbds.api.commands.Command::getName).toList()) + ">";
//        }
//
//        else {
//            return command.getName() + " " + String.join(" ", command.arguments().stream().map(this::wrapArgument).toList());
//        }

        return "<null/usage>"; // TODO Тимчасова заглушка.

    }


    private String wrapArgument(@NotNull CommandArgument argument) {
        return argument.required() ? "<" + argument.name() + ">" : "[" + argument.name() + "]";
    }


}
