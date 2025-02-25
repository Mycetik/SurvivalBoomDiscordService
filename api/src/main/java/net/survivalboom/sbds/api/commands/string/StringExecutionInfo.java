package net.survivalboom.sbds.api.commands.string;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.ExecutionInfo;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class StringExecutionInfo extends ExecutionInfo {

    public StringExecutionInfo(@NotNull Command command, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {
        super(command, alias, arguments, logger, sbds);
    }

}
