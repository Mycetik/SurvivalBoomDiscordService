package net.survivalboom.sbds.modules.chatbot.commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.survivalboom.sbds.api.commands.argument.discord.channel.TextChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.BooleanArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.modules.chatbot.storage.AIChannels;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "channel", description = "Allow chatbot to moderate specified channel", usage = "allow <channel> <value>")
public class ChannelCommand extends CommandBase implements ConsoleCommandExecutor {

    private final AIChannels channels;

    public ChannelCommand(@NotNull AIChannels channels) {
        this.channels = channels;
    }


    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        TextChannel channel = info.arguments().getCastNotNull("channel", TextChannel.class);
        Boolean value = info.arguments().getCastOrNull("value", Boolean.class);

        if (value == null) {
            boolean v = channels.isAiChannel(channel).join();
            info.logger().info("Channel `{}` value: `{}`", channel.getName(), v);
            return;
        }

        channels.setAiChannel(channel, value).join();
        info.logger().info("Successfully set `{}` to `{}`.", channel.getName(), value);

    }


    @ArgumentMethod(name = "channel")
    public TextChannelArgument channel() {
        return new TextChannelArgument();
    }

    @ArgumentMethod(name = "value", required = false)
    public BooleanArgument value() {
        return new BooleanArgument();
    }

}
