package net.survivalboom.sbds.modules.test.commands;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.commands.string.StringCommandExecutor;
import net.survivalboom.sbds.api.commands.string.StringExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

@CommandClass(name = "ban-prototype", description = "A test command that acts like a ban command to test advanced features of SBDS", permission = "testmodule.commands.ban")
public class BanPrototypeCommand extends CommandBase implements SlashCommandExecutor, StringCommandExecutor {

    @Override
    public void executes(@NotNull StringExecutionInfo info) {
        User user = info.arguments().getCast("user", User.class).orElseThrow();
        executes0(info, user);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        User user = info.arguments().getCast("user", User.class).orElseThrow();
        executes0(info, user);
    }

    private void executes0(@NotNull InteractionHolder info, @NotNull User user) {

        AtomicBoolean confirmed = new AtomicBoolean(false);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        info.reply("testmodule.command.ban.confirm")
                .withPlaceholders("user", user)
                .buttonCallback("confirm", true, click -> {

                    if (cancelled.get()) {
                        return;
                    }

                    info.reply("testmodule.command.ban.success")
                            .withPlaceholders("user", user)
                            .queue();

                }, null,30000)
                .buttonCallback("cancel", true, click -> {

                    if (confirmed.get()) {
                        return;
                    }

                    info.reply("testmodule.command.ban.cancelled")
                            .withPlaceholders("user", user)
                            .queue();

                }, null, 30000)
                .queue();

    }

    @ArgumentMethod
    public UserArgument user() {
        return new UserArgument();
    }

}
