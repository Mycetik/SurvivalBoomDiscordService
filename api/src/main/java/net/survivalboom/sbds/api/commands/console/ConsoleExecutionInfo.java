package net.survivalboom.sbds.api.commands.console;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ConsoleExecutionInfo extends CommandExecutionInfo {

    private final String input;

    public ConsoleExecutionInfo(@NotNull Command command, @NotNull String input, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {
        super(command, alias, arguments, logger, sbds);
        this.input = input;
    }

    public @NotNull String input() {
        return input;
    }

}
