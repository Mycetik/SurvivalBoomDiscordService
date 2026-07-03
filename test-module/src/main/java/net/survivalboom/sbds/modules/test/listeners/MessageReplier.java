package net.survivalboom.sbds.modules.test.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfig;
import net.survivalboom.sbds.api.events.*;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReplier implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(MessageReplier.class);
    private final ModuleMain module;

    public MessageReplier(ModuleMain module) {
        this.module = module;
    }

    @EventHandler
    public void onMessage(@NotNull MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        IGuildConfig config = module.getGuildConfig().obtainConfig(event.getGuild());
        TextChannel channel = config.get("replier", TextChannel.class).join().orElse(null);
        if (channel == null || !event.getChannel().equals(channel)) {
            return;
        }

        Message message = event.getMessage();
        message.reply(message.getContentRaw()).queue();

        module.callEvent(new MyTestEvent(module, message));

    }

    @EventHandler
    public void onTestEvent(@NotNull MyTestEvent event) {
        log.info("TEST EVENT! `{}`", event.message.getContentRaw());
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTestEvent2(@NotNull MyTestEvent event) {
        log.info("TEST EVENT 2! `{}`", event.message.getContentRaw());
    }

    public static class MyTestEvent extends EventCancellableBase {

        private final Message message;

        public MyTestEvent(@NotNull ModuleMain module, @NotNull Message message) {
            super(module);
            this.message = message;
        }

        public @NotNull Message getMessage() {
            return message;
        }

    }

}
