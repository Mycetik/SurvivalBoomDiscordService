package net.survivalboom.sbds.modules.chatbot.commands;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.modules.chatbot.storage.AIChannels;
import org.jetbrains.annotations.NotNull;

@Command(name = "chatbot", description = "The main command of the ChatBot module")
public class ChatBotCommand extends CommandBase implements ConsoleCommand {

    public ChatBotCommand(@NotNull AIChannels channels) {
        addSubCommand(new ChannelCommand(channels));
    }

}
