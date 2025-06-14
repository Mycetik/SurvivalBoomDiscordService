package net.survivalboom.sbds.modules.chatbot.listener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.scheduler.IScheduler;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.modules.chatbot.chats.ChatManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GuildEventsListener extends Manager implements Listener {

    private static final Logger log = LoggerFactory.getLogger(GuildEventsListener.class);
    private final ChatManager chatManager;

    private final IScheduler scheduler;


    private final Map<TextChannel, Long> lastMessageTime = new WeakHashMap<>();

    private final Map<TextChannel, List<Message>> queueMap = new WeakHashMap<>();

    private final List<Guild> guildLock = new ArrayList<>();


    private JDA bot;

    private ISchedulerTask task;


    public GuildEventsListener(@NotNull ChatManager chatManager) {
        this.chatManager = chatManager;
        this.scheduler = chatManager.getModule().getSbds().getScheduler();
    }

    @Override
    protected void init0() {
        task = scheduler.schedule(chatManager.getModule(), this::task, 10, 1000);
        this.bot = chatManager.getBot();
    }

    @Override
    protected void shutdown0() {
        if (task == null) return;
        task.cancelAndWait(5000, true);
        task = null;
    }

    private void task() {

        chatManager.removeUnusedChats();

        for (Map.Entry<TextChannel, List<Message>> entry : queueMap.entrySet()) {

            TextChannel channel = entry.getKey();

            if (chatManager.toStop().contains(channel)) {
                log.info("Stopped conversation with bot.");
                chatManager.toStop().remove(channel);
                continue;
            }

            if (!checkDelay(channel)) return;
            if (guildLock.contains(channel.getGuild())) continue;

            List<Message> messages = new ArrayList<>(entry.getValue());
            queueMap.remove(channel);

            guildLock.add(channel.getGuild());

            chatManager.react(channel, messages).thenAccept(v -> guildLock.remove(channel.getGuild()));

        }

    }

    @EventHandler
    public void onMessage(MessageReceivedEvent event) {

        if (!(event.getChannel() instanceof TextChannel channel)) {
            return;
        }

        if (event.isFromThread() || event.isWebhookMessage() || event.getAuthor().isBot()) {
            return;
        }

        if (!chatManager.allowedChannels().isAllowedChannel(channel)) {
            return;
        }

        if (!checkDelay(channel)) {
            return;
        }

        Message message = event.getMessage();

        if (!chatManager.isGuildAllowed(message.getGuild())) {
            return;
        }

        if (message.getContentRaw().isEmpty()) {
            return;
        }

        boolean hasCharacterName = chatManager.getCharacterNames().stream().anyMatch(s -> event.getMessage().getContentRaw().toLowerCase().contains(s.toLowerCase()));
        boolean hasMention = message.getMentions().isMentioned(bot.getSelfUser(), Message.MentionType.USER);

        MessageReference reference = message.getMessageReference();
        boolean botMessageReplied = reference != null && reference.getMessage().getAuthor().equals(bot.getSelfUser());

        if (chatManager.getChat(channel) == null && !(hasCharacterName || hasMention || botMessageReplied)) {
            return;
        }

        if (chatManager.bannedUsers().isUserBanned(message.getGuild(), message.getAuthor())) {
            return;
        }

        queueMap.computeIfAbsent(channel, key -> new ArrayList<>()).add(event.getMessage());
        lastMessageTime.put(channel, System.currentTimeMillis());

    }


    private boolean checkDelay(TextChannel channel) {

        if (!lastMessageTime.containsKey(channel)) {
            return true;
        }

        long timestamp = lastMessageTime.get(channel);
        return System.currentTimeMillis() - timestamp > 3000;

    }

    private void putLastMessageTimestamp(TextChannel channel) {
        lastMessageTime.put(channel, System.currentTimeMillis());
    }


}
