package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;

@Command(name = "test", description = "Тут має бути текст", permission = "testmodule.command.testcommand", defaultPermission = true)
public class TestCommand extends CommandBase implements SlashCommand {

    public TestCommand(ModuleMain moduleMain) {

        moduleMain.getSbds().getButtonInteractionManager().registerButton(moduleMain.getModule(), "ban", this::button);

    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        info.reply("commands.sbds.status").queue();

    }

    private void button(@NotNull ButtonInteractionInfo info) {

        info.replyRaw("ТЫ ЕБЛАААААН, ТЫ ЗАБАНЕН БЛЯЯЯЯТЬ, Я НЕ БУДУ ТЕБЯ РАЗБАНИВАТЬ, ПОТОМУ ЧТО ТЫ ТУПОЙ СУКА!!!").queue();

    }

}
