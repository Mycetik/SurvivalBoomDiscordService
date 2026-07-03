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
import net.survivalboom.sbds.api.interaction.component.ComponentInteractionRequest;
import org.jetbrains.annotations.NotNull;

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

        info.reply("testmodule.command.ban.confirm")
                .withPlaceholders("user", user)
                .withComponents(builder -> {

                    builder.addButton("confirm", ComponentInteractionRequest.ExpireMode.ALL, click ->
                        info.reply("testmodule.command.ban.success")
                                .withPlaceholders("user", user)
                                .queue()
                    );

                    builder.addButton("cancel", ComponentInteractionRequest.ExpireMode.ALL, click ->
                            info.reply("testmodule.command.ban.cancelled")
                                    .withPlaceholders("user", user)
                                    .queue()
                    );

                    builder
                        .setExpireAction(info::invalidateInputs)
                        .setExpireInterval(30);
                })
                .queue();

    }

    @ArgumentMethod
    public UserArgument user() {
        return new UserArgument();
    }

}
