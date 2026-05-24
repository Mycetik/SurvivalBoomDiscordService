package net.survivalboom.sbds.core.commands.console;

import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.console.IConsoleListener;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.commands.AbstractCommandManager;
import net.survivalboom.sbds.core.commands.cmds.common.StatusCommand;
import net.survivalboom.sbds.core.commands.cmds.console.ServersCommand;
import net.survivalboom.sbds.core.commands.parser.ArgumentParsingException;
import net.survivalboom.sbds.core.commands.parser.StringCommandParser;
import net.survivalboom.sbds.core.commands.cmds.console.HelpCommand;
import net.survivalboom.sbds.core.commands.cmds.console.ShutdownCommand;
import net.survivalboom.sbds.core.commands.cmds.console.modules.ModulesCommand;
import net.survivalboom.sbds.core.scheduler.SchedulerTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class ConsoleListener extends AbstractCommandManager<IConsoleListener.IRegisteredConsoleCommand, IConsoleListener> implements IConsoleListener {

    private final Scanner scanner = new Scanner(System.in);

    private SchedulerTask task;


    public ConsoleListener(@NotNull SBDS sbds) {
        super(sbds);
    }


    @Override
    protected void init0() {

        super.init0();

        registerCommand0(null, new ShutdownCommand().build());
        registerCommand0(null, new HelpCommand().build());
        registerCommand0(null, new ModulesCommand().build());

        registerCommand0(null, new StatusCommand().build());
        registerCommand0(null, new ServersCommand().build());

        task = sbds.getScheduler().schedule0(null, "ConsoleListener", task -> this.consoleListener(), 0, 50);

    }

    @Override
    protected void shutdown0() {

        task.tryCancel();
        task = null;

        super.shutdown0();

    }

    @Override
    protected @NotNull ConsoleListener.RegisteredConsoleCommand createCommandReg(@NotNull Command command) {
        return new RegisteredConsoleCommand(this, command);
    }


    private void consoleListener() {

        if (!scanner.hasNext() || !sbds.isReady()) {
            return;
        }

        String input = scanner.nextLine().strip();

        try {
            processCommand(input);
        }

        catch (Throwable t) {
            logger.error("Ooopsies! A fatal internal error occurred while attempting to process input `{}`. OutOfMemoryError?", input);
        }

    }

    private void processCommand(@NotNull String input) {

        String prefix = StringCommandParser.getPrefix(input);

        var cmdReg = getByAlias(prefix);
        if (cmdReg == null) {
            rootLogger.info("Unknown command. Type 'help' to view all available commands.");
            return;
        }

        Command command = cmdReg.getCommand();
        String string = input.substring(prefix.length()).strip();

        try {
            processSubcommand(prefix, string, input, cmdReg, command);
        }

        catch (Throwable t) {
            logger.error("An internal error occurred while attempting to perform console command `{}`.", input, t);
        }

    }

    private void processSubcommand(
            @NotNull String alias,
            @NotNull String remaining,
            @NotNull String fullInput,
            @NotNull IRegisteredConsoleCommand rootCommand,
            @NotNull Command command
    ) {

        Function<CommandArgument, ArgumentParsingContext> argCtxCreator = argument -> new ArgumentParsingContext(rootCommand, command, argument);

        var arguments = command.getArguments();

        StringCommandParser.Result parsingResult;
        try {
            parsingResult = StringCommandParser.parseInput(remaining, ArgumentScope.CONSOLE, arguments, argCtxCreator);
        }

        catch (ArgumentParsingException e) {

            CommandArgument argument = e.getArgument();

            String argInput = e.getInput();
            String argName = argument.name();

            Throwable cause = e.getCause();
            if (!(cause instanceof ArgumentParseException ape)) {
                rootLogger.error("Ooopsies! A fatal internal error occurred while attempting to parse argument `{}` from `{}`. This is an internal argument `{}` error, not command error.", argName, argInput, argument.argument());
                return;

            }

            rootLogger.error("Invalid input `{}` for argument `{}`: {}", argInput, argName, ape.getMessage());

            return;

        }

        long requiredArgumentsSize = arguments.stream()
                .filter(CommandArgument::required)
                .count();

        int currentArgumentsSize = parsingResult.arguments().size();

        if (currentArgumentsSize < requiredArgumentsSize) {
            rootLogger.info("Incorrect or incomplete command. Expected {} arguments, got {}. Usage: `{}`", requiredArgumentsSize, currentArgumentsSize, "null");
        }

        TypeMap ctxArgs = parsingResult.arguments();
        ConsoleExecutionInfo info = new ConsoleExecutionInfo(rootCommand, command, fullInput, alias, ctxArgs, logger);

        try {

            ConsoleCommandExecutor consoleCommandExecutor = (ConsoleCommandExecutor) command.getExecutor();
            consoleCommandExecutor.executes(info);

        }

        catch (Throwable t) {
            logger.error("An unknown error occurred in command `{}`. Command threw an exception.", command.getName(), t);
            return;
        }

        var subCommandsArgs = parsingResult.arguments2().entrySet().stream()
                .filter(entry -> entry.getKey().argument() instanceof SubCommandArgument)
                .toList();

        if (!subCommandsArgs.isEmpty()) {

            for (var entry : subCommandsArgs) {

                SubCommandArgument.SubCommand subcommand = (SubCommandArgument.SubCommand) entry.getValue();

                processSubcommand(subcommand.alias(), parsingResult.remaining(), fullInput, rootCommand, subcommand.command());

            }

        }

    }

    public static class RegisteredConsoleCommand extends RegisteredCommand<IRegisteredConsoleCommand, IConsoleListener> implements IRegisteredConsoleCommand {

        public RegisteredConsoleCommand(@NotNull ConsoleListener manager, @NotNull Command command) {
            super(manager, command);
        }

    }


}
