package net.survivalboom.sbds.modules.chatbot.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.Manager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MessageBuffer extends Manager {


    // Інтервали виміряються у секундах!!!
    public static final int MIN_INTERVAL = 15;

    public static final int MAX_INTERVAL = 600;

    public  static final double MAX_MSGS_PER_MIN = 200.0;
    private static final Logger log = LoggerFactory.getLogger(MessageBuffer.class);


    private final ModuleMain module;

    private final Consumer<Push> callback;


    private final Map<MessageChannel, List<Message>> buffer = new ConcurrentHashMap<>();

    private final Map<MessageChannel, Long> lastPush = new HashMap<>();


    private ISchedulerTask task;


    public MessageBuffer(
            @NotNull Consumer<Push> callback,
            @NotNull ModuleMain module
    ) {
        this.callback = callback;
        this.module = module;
    }


    @Override
    protected void init0() {
        this.task = module.getSbds().getScheduler().schedule(module, this::task, 0, 1000);
    }

    @Override
    protected void shutdown0() {
        this.task.cancelAndWait(5000, false);
        this.task = null;
    }


    private void task() {

        var map = new HashMap<>(buffer);
        for (var entry : map.entrySet()) {

            MessageChannel channel = entry.getKey();
            List<Message> messages = entry.getValue();

            if (messages.isEmpty()) {
                buffer.remove(channel);
                return;
            }

            int mpm = calculateMessagesPerMinute(messages);
            int pushInterval = createPushInterval(mpm);

            long lastPushTime = lastPush.getOrDefault(channel, -1L);
            long currentTimeSeconds = System.currentTimeMillis() / 1000;

            log.info("MPM: {}; INTERVAL: {}; LAST PUSH: {}", mpm, pushInterval, lastPushTime);

            if (mpm > 5 && lastPushTime != -1 && currentTimeSeconds - lastPushTime < pushInterval) {
                return;
            }

            lastPush.put(channel, currentTimeSeconds);

            this.buffer.remove(channel);

            try {
                callback.accept(new Push(messages, channel, mpm, pushInterval));
            }

            catch (Exception e) {
                log.error("Failed to push {} messages from buffer!", messages.size(), e);
            }

        }

    }

    public void addMessage(@NotNull Message message) {
        this.buffer.computeIfAbsent(message.getChannel(), k -> new ArrayList<>()).add(message);
    }

    private int calculateMessagesPerMinute(@NotNull List<Message> messages) {

        long currentTime = System.currentTimeMillis() / 1000;
        long minimumTimeOffset = currentTime - 60;

        return (int) messages.stream().filter(m -> m.getTimeCreated().toEpochSecond() > minimumTimeOffset).count();

    }

    private int createPushInterval(int mpm) {

        if (mpm < 0) {
            throw new IllegalArgumentException("mpm < 0");
        }

        double ratio = Math.log(1 + mpm) / Math.log(1 + MAX_MSGS_PER_MIN);
        double interval = MAX_INTERVAL - (MAX_INTERVAL - MIN_INTERVAL) * ratio;

        return (int) Math.round(Math.max(MIN_INTERVAL, Math.min(MAX_INTERVAL, interval)));

    }


    public record Push(@NotNull List<Message> messages, @NotNull MessageChannel channel, int mpm, int currentInterval) {}

}
