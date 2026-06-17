package net.survivalboom.sbds.modules.test.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfig;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;

public class MessageReplier implements EventListener {

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

    }

}
