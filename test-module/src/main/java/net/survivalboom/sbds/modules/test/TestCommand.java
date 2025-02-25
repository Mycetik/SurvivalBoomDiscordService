package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.console.ConsoleCommand;
import net.survivalboom.sbds.api.console.ConsoleExecutionInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "test", description = "Рисует большой жЫрный член.", usage = "test [hui]")
public class TestCommand extends CommandBase implements ConsoleCommand, SlashCommand {

    private static final Logger log = LoggerFactory.getLogger(TestCommand.class);

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        log.info(info.toString());
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        info.interaction().reply("Шо ты лысы, плаки-плаки? " + info.arguments().getByName("test1")).queue();
    }

    @CommandArgument(name = "test1", description = "Плаки-текст")
    public Argument<?> test0() {
        return new StringArgument();
    }

}
