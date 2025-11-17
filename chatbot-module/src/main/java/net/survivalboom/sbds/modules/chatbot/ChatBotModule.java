package net.survivalboom.sbds.modules.chatbot;

import io.github.sashirestela.openai.SimpleOpenAI;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.modules.ai.utils.AIQueue;
import net.survivalboom.sbds.modules.chatbot.bot.ChatBot;
import net.survivalboom.sbds.modules.chatbot.bot.MessageListener;
import net.survivalboom.sbds.modules.chatbot.commands.ChatBotCommand;
import net.survivalboom.sbds.modules.moderation.api.IModerationModule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChatBotModule extends ModuleMain {

    public static NamespacedKey KEY;

    private static ChatBotModule instance;


    private SimpleOpenAI aiManager;

    private AIQueue aiQueue;

    private IModerationModule moderationModule;

    private ChatBot chatBot;

    private MessageListener messageListener;


    @Override
    public void onEnable() {

        saveDefaultConfig();

        KEY = NamespacedKey.fromModule(this, "data");

        this.aiManager = getService(SimpleOpenAI.class);
        Objects.requireNonNull(aiManager, "failed to get ai service");

        this.aiQueue = getService(AIQueue.class);
        Objects.requireNonNull(aiQueue, "failed to get ai queue");

        this.moderationModule = getService(IModerationModule.class);
        Objects.requireNonNull(moderationModule, "failed to get moderation service");

        this.chatBot = new ChatBot(this);
        chatBot.init();

        this.messageListener = new MessageListener(this);
        messageListener.init();

        registerCommand(new ChatBotCommand(chatBot.getChannels()));

        instance = this;

    }

    @Override
    public void onDisable() {

        this.aiManager = null;

        chatBot.shutdown();
        chatBot = null;

        messageListener.shutdown();
        messageListener = null;

        moderationModule = null;
        aiManager = null;

        instance = null;

    }


    public @NotNull SimpleOpenAI getAiManager() {
        return aiManager;
    }

    public @NotNull AIQueue getAiQueue() {
        return aiQueue;
    }

    public @NotNull ChatBot getChatBot() {
        return chatBot;
    }

    public @NotNull IModerationModule getModerationModule() {
        return moderationModule;
    }


    public static @NotNull ChatBotModule getInstance() {
        return instance;
    }


}
