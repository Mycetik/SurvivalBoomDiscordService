package net.survivalboom.sbds.modules.privatevoice;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class PrivateVoice {
    private final VoiceChannel channel;
    private Member owner;

    public PrivateVoice(VoiceChannel channel) {
        this.channel = channel;
    }

    public VoiceChannel getChannel() {
        return channel;
    }

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }
}
