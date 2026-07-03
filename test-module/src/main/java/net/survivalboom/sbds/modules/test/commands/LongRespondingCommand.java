package net.survivalboom.sbds.modules.test.commands;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.commands.string.StringCommandExecutor;
import net.survivalboom.sbds.api.commands.string.StringExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "test-long-respond", description = "Test a long-responding command")
public class LongRespondingCommand extends CommandBase implements SlashCommandExecutor, StringCommandExecutor {

    @Override
    public void executes(@NotNull StringExecutionInfo info) {
        executes0(info);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        executes0(info);
    }

    private void executes0(@NotNull InteractionHolder info) {

        // Симулюємо лаги, чекаємо 5 секунд, SBDS надішле "Бот думає..."
        CommonUtils.sleep(5000);

        // Надсилаємо шось.
        info.reply("testmodule.command.long-responding.first").queue();

        CommonUtils.sleep(2000);

        // Ми тут тіпа симулюємо "бурнаю дєятєльность". Повідомлення має редагуватись.
        int attempts = 10;
        while (attempts >= 0) {

            info.reply("testmodule.command.long-responding.loop").withPlaceholders("index", attempts).queue();

            attempts--;
            CommonUtils.sleep(1000);

        }

        info.reply("testmodule.command.long-responding.success").queue();

    }

}
