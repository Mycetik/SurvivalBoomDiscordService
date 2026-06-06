package net.survivalboom.sbds.modules.test.commands;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "test-ephemeral", description = "Test an ephemeral replies", ephemeral = true)
public class EphemeralCommand extends CommandBase implements SlashCommandExecutor {

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        info.reply("testmodule.command.ephemeral-test").queue();
    }

}
