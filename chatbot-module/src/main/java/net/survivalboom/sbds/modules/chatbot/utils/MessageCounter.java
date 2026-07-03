package net.survivalboom.sbds.modules.chatbot.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.valid.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageCounter extends Manager {

    private final ModuleMain module;


    private final Map<MessageChannel, AtomicInteger> counters = new ConcurrentHashMap<>();

    private final Map<MessageChannel, Integer> messagesPerMinute = new ConcurrentHashMap<>();


    private ISchedulerTask task;

    private final int period;


    public MessageCounter(@NotNull ModuleMain module, int period) {
        this.module = module;
        this.period = period;
    }


    @Override
    protected void init0() {
        this.task = module.getSbds().getScheduler().schedule(module, "MessageCounter", this::task, 0, period);
    }

    @Override
    protected void shutdown0() {
        this.task.cancelAndWait(1000, false);
        this.task = null;
    }

    private void task() {

        for (var entry : counters.entrySet()) {

            var channel = entry.getKey();
            var counter = entry.getValue();

            int messages = counter.get();
            counter.set(0);

            messagesPerMinute.put(channel, messages);

        }

    }


    public void count(@NotNull Message message) {
        MessageChannel channel = message.getChannel();
        counters.computeIfAbsent(channel, k -> new AtomicInteger()).addAndGet(1);
    }

    public int getCount(@NotNull TextChannel channel) {

        if (!messagesPerMinute.containsKey(channel)) {
            return -1;
        }

        return messagesPerMinute.get(channel);
        
    }

}
