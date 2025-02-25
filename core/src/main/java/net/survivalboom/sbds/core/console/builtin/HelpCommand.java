package net.survivalboom.sbds.core.console.builtin;

import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.ICommandManager;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.console.ConsoleCommand;
import net.survivalboom.sbds.api.console.ConsoleExecutionInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Command(name = "help", description = "Shows a list of available commands")
public class HelpCommand extends CommandBase implements ConsoleCommand {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        Logger logger = info.logger();

        Map<String, List<ICommandManager.RegisteredCommand>> registeredCommandMap = info.sbds().getConsoleListener().getRegisteredCommands().stream().collect(Collectors.groupingBy(c -> c.registrar() != null ? c.registrar().getName() : "Default"));

        logger.info(" ");
        logger.info("---- < Available Commands List > ----");

        registeredCommandMap.forEach((registrar, commands) -> {
            logger.info("{}:", registrar);
            commands.forEach(command -> logger.info(formatCommand(command.command())));
        });

        logger.info(" ");
        logger.info("* <> - Required; [] - Optional;");
        logger.info("---- ---- ---- ------- ---- ---- ----");

    }

    private String formatCommand(net.survivalboom.sbds.api.commands.Command command) {
        String usage = genUsage(command);
        String description = Objects.requireNonNullElse(command.description(), "Command has no description.");
        return String.format("> %s - %s", usage, description);
    }

    private @NotNull String genUsage(@NotNull net.survivalboom.sbds.api.commands.Command command) {

        String usage = command.usage();
        if (usage != null) {
            return usage;
        }

        if (command.hasSubcommands()) {
            return command.getName() + " " + "<" + String.join("/", command.subcommands().stream().map(net.survivalboom.sbds.api.commands.Command::getName).toList()) + ">";
        }

        else {
            return command.getName() + " " + String.join(" ", command.arguments().stream().map(this::wrapArgument).toList());
        }

    }


    private String wrapArgument(@NotNull CommandArgument argument) {
        return argument.required() ? "<" + argument.name() + ">" : "[" + argument.name() + "]";
    }


}
