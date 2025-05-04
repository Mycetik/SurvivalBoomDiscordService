package net.survivalboom.sbds.modules.test;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.TextChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import org.jetbrains.annotations.NotNull;

@Command(name = "say", description = "Надсилає повідомлення у канал від імені головного бота.", usage = "say <Канал> <Повідомлення>")
public class SayCommand extends CommandBase implements ConsoleCommand, SlashCommand {


    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        TextChannel channel = info.arguments().get("channel", TextChannel.class);
        String message = info.arguments().get("message", String.class);

        assert channel != null;
        assert message != null;

        channel.sendMessage(message).complete();

        info.logger().info("Sent `{}` to {} successfully!", message, channel.getName());

    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        TextChannel channel = info.arguments().get("channel", TextChannel.class);
        String message = info.arguments().get("message", String.class);

        assert channel != null;
        assert message != null;

        channel.sendMessage(message).queue();

        info.interaction().reply(":pig:").queue();

    }

    @CommandArgument(name = "channel")
    public Argument<?> channel() {
        return new TextChannelArgument();
    }

    @CommandArgument(name = "message", index = 1)
    public Argument<?> message() {
        return new StringArgument();
    }

}
