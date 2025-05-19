package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import org.jetbrains.annotations.NotNull;

@Command(name = "test", description = "Рисует большой жЫрный член.", permission = "testmodule.command.testcommand", defaultPermission = true)
public class TestCommand extends CommandBase implements SlashCommand {

    private final IModalInteractionManager.IRegisteredModal modal;

    public TestCommand(IModalInteractionManager.IRegisteredModal modal) {
        this.modal = modal;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        modal.open(info.interaction(), null).thenAccept(modal -> {
            modal.reply(modal.values().toString()).queue();
        });

    }

    @CommandArgument(name = "key", required = false)
    public Argument<?> key() {
        return new StringArgument();
    }

    @CommandArgument(name = "value", required = false)
    public Argument<?> value() {
        return new StringArgument();
    }

}
