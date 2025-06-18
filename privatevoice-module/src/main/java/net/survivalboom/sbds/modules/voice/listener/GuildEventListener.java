package net.survivalboom.sbds.modules.voice.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.modules.voice.voice.PrivateVoice;
import net.survivalboom.sbds.modules.voice.voice.VoiceManager;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public class GuildEventListener implements Listener {

    private final VoiceManager voiceManager;


    public GuildEventListener(@NotNull VoiceManager voiceManager) {
        this.voiceManager = voiceManager;
    }


    @EventHandler
    public void onVoiceStateUpdate(GuildVoiceUpdateEvent event) {

        if (voiceManager.isShuttingDown()) {
            return;
        }

        if (!(event.getNewValue() instanceof VoiceChannel channel)) {
            return;
        }

        if (!voiceManager.voiceCreatorChannels().isVoiceCreator(channel)) {
            return;
        }

        Member member = event.getMember();

        if (voiceManager.getIgnoredMembers().contains(member)) {
            return;
        }

        voiceManager.createVoice(member, channel);

    }

    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!(event.getChannel() instanceof VoiceChannel channel)) {
            return;
        }

        if (event.getAuthor().isBot()) {
            return;
        }

        PrivateVoice voice = voiceManager.findByChannel(channel);
        if (voice == null) {
            return;
        }

        if (!voice.getChannel().getMembers().contains(event.getMember())) {
            return;
        }

        if (!event.getMessage().getContentRaw().equals("+")) {
            return;
        }

        channel.getHistory().retrievePast(100).queue(messages -> {

            List<Message> toDelete = messages.stream()
                    .filter(m -> !m.isPinned())
                    .filter(m -> m.getTimeCreated().isAfter(OffsetDateTime.now().minusDays(14)))
                    .toList();

            if (!toDelete.isEmpty()) {
                channel.deleteMessages(toDelete).queue(v -> voice.updateControlPanel(true));
            }

        });

    }


}
