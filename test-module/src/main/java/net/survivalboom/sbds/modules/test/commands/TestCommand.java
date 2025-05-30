package net.survivalboom.sbds.modules.test.commands;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Command(name = "test", description = "Command to test various SBDS functions")
public class TestCommand extends CommandBase implements SlashCommand {


    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        addSubCommand(new ModalTestCommand());
    }

}
