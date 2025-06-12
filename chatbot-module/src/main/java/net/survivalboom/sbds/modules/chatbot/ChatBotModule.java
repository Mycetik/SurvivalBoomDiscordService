package net.survivalboom.sbds.modules.chatbot;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.chatbot.ai.OpenAiManager;
import net.survivalboom.sbds.modules.chatbot.chats.ChatManager;
import net.survivalboom.sbds.modules.chatbot.commands.BanUserCommand;
import net.survivalboom.sbds.modules.chatbot.commands.SetChannelCommand;
import net.survivalboom.sbds.modules.chatbot.listener.GuildEventsListener;

import java.io.File;
import java.util.Map;

public class ChatBotModule extends ModuleMain {

    private OpenAiManager openAiManager;

    private ChatManager chatManager;

    private GuildEventsListener guildEventsListener;

    @Override
    public void onLoad() {
        openAiManager = new OpenAiManager(new File(getDataFolder(), "openai-token"));
        chatManager = new ChatManager(this, openAiManager);
        guildEventsListener = new GuildEventsListener(chatManager);
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        checkFiles(Map.of(
                "translations/translation_uk.yml", "translation/translation_uk.yml"
        ));
        addModuleTranslations();

        openAiManager.init();
        if (!openAiManager.isEnabled()) return;

        chatManager.init();
        if (!chatManager.isEnabled()) return;

        guildEventsListener.init();

        registerEvents(guildEventsListener);

        registerSlashCommand(new SetChannelCommand(chatManager.allowedChannels()));
        registerSlashCommand(new BanUserCommand(chatManager.bannedUsers()));

    }

    @Override
    public void onDisable() {

        guildEventsListener.shutdownIfNeeded();
        chatManager.shutdownIfNeeded();
        openAiManager.shutdownIfNeeded();

    }

}
