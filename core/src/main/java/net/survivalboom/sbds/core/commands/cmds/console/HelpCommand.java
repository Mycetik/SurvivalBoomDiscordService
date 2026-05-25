package net.survivalboom.sbds.core.commands.cmds.console;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
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

    private String formatCommand(Command command) {
        String usage = command.getArguments().isEmpty() ? "" : " " + genUsage(command);
        String description = Objects.requireNonNullElse(command.getDescription(), "Command has no description.");
        return String.format("> %s%s - %s", command.getName(), usage, description);
    }

    private @NotNull String genUsage(@NotNull Command command) {

        List<String> stringList = new ArrayList<>();
        for (CommandArgument argument : command.getArguments()) {

            String name;
            if (argument.isSubCommand()) {
                List<String> strings = ((SubCommandArgument) argument.argument()).getSubcommands().stream().map(Command::getName).toList();
                name = String.join("/", strings);
            }

            else {
                name = argument.name();
            }

            if (argument.required()) {
                stringList.add("<" + name + ">");
            }

            else {
                stringList.add("[" + name + "]");
            }

        }

        return String.join(" ", stringList);

    }


    private String wrapArgument(@NotNull CommandArgument argument) {
        return argument.required() ? "<" + argument.name() + ">" : "[" + argument.name() + "]";
    }


}
