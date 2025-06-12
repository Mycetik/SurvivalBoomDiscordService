package net.survivalboom.sbds.modules.chatbot.commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.TextChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.BooleanArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.chatbot.storage.AllowedChannels;
import org.jetbrains.annotations.NotNull;

@Command(name = "chatbot-set", description = "Allow/Deny chatbot talk in specified channel.", permission = "chatbot.command.set")
public class SetChannelCommand extends CommandBase implements SlashCommand {

    private final AllowedChannels allowedChannels;


    public SetChannelCommand(@NotNull AllowedChannels allowedChannels) {
        this.allowedChannels = allowedChannels;
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        TextChannel textChannel = info.arguments().getCastOrNull("channel", TextChannel.class);
        if (textChannel == null) {
            info.reply("chatbot.command.set.invalid-channel").queue();
            return;
        }

        boolean value = info.arguments().getCastOrDefault("value", Boolean.class, true);

        allowedChannels.setChannelAllowed(textChannel, value);

        String str = value ? "chatbot.command.set.allow" : "chatbot.command.set.deny";
        info.reply(str).withPlaceholders("{CHANNEL}", textChannel.getAsMention()).queue();

    }

    @CommandArgument(name = "channel", description = "A channel")
    public Argument<?> channel() {
        return new TextChannelArgument();
    }

    @CommandArgument(name = "value", description = "Allow/Deny", index = 1)
    public Argument<?> value() {
        return new BooleanArgument();
    }

}
