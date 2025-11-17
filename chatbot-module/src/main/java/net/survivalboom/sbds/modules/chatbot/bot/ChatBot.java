package net.survivalboom.sbds.modules.chatbot.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.modules.chatbot.ChatBotModule;
import net.survivalboom.sbds.modules.chatbot.storage.AIChannels;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ChatBot extends Manager {

    private static final Logger log = LoggerFactory.getLogger(ChatBot.class.getSimpleName());

    private final ChatBotModule module;


    private final AIChannels channels;


    private JDA bot;


    private String character;

    private String prompt;


    public ChatBot(@NotNull ChatBotModule module) {
        this.module = module;
        this.channels = new AIChannels(module);
    }


    @Override
    protected void init0() {

        File tokenFile = new File(module.getDataFolder(), "chatbot-token");
        if (!tokenFile.exists()) {

            try {
                tokenFile.createNewFile();
            }

            catch (IOException e) {
                throw new RuntimeException(e);
            }

            log.error("Token file does not exist. Please provide the discord bot token in `{}`.", tokenFile.getAbsolutePath());
            throw new RuntimeException();

        }

        if (!tokenFile.isFile()) {
            log.error("Invalid token file. Please provide the discord bot token in `{}`.", tokenFile.getAbsolutePath());
            throw new RuntimeException();
        }

        String token;
        try (InputStream stream = new FileInputStream(tokenFile)) {
            token = new String(stream.readAllBytes());
        }

        catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (token.isBlank()) {
            log.error("Empty token file. Please provide the discord bot token in `{}`.", tokenFile.getAbsolutePath());
            throw new RuntimeException();
        }

        log.info("Logging in...");

        this.bot = JDABuilder.createLight(token).build();
        try {
            bot.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("Successfully logged in! ({})", bot.getSelfUser().getEffectiveName());

        character = module.getConfig().getString("character");
        prompt = module.getConfig().getString("prompt");

    }

    @Override
    protected void shutdown0() {

        log.info("Shutting down chatbot...");

        this.bot.shutdown();

    }


    public @NotNull AIChannels getChannels() {
        return channels;
    }


    public @NotNull JDA getBot() {
        return bot;
    }


    public @NotNull String getCharacter() {
        return character;
    }

    public @NotNull String getPrompt() {
        return prompt;
    }


}
