package net.survivalboom.sbds.modules.chatbot.commands;

import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.modules.chatbot.storage.AIChannels;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "chatbot", description = "The main command of the ChatBot module")
public class ChatBotCommand extends CommandBase implements ConsoleCommandExecutor {

    public ChatBotCommand(@NotNull AIChannels channels) {
        addSubCommand(new ChannelCommand(channels));
    }

}
