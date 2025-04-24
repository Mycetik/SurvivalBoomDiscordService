package net.survivalboom.sbds.modules.privatevoice;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.survivalboom.sbds.api.utils.TypeMap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class PrivateVoice {
    private final VoiceChannel channel;
    private Member owner;

    private TypeMap settings;

    public PrivateVoice(VoiceChannel channel) {
        this.channel = channel;
    }

    public void setup(TypeMap map) {
        VoiceChannelManager manager = getChannel().getManager();
        settings = map;

        int max = map.getCastOrDefault("max", Integer.class, 20);
        manager.setUserLimit(max).queue();

        List<Long> blacklist = map.getCastOrDefault("blacklist", List.class, Collections.emptyList());
        Guild guild = getChannel().getGuild();

        for (Long userId : blacklist) {
            if (userId == null) continue;

            Member member = guild.getMemberById(userId);
            if (member != null) {
                getChannel().upsertPermissionOverride(member)
                        .deny(Permission.VOICE_CONNECT)
                        .queue();
            }
        }

        List<Long> whitelist = map.getCastOrDefault("whitelist", List.class, Collections.emptyList());

        for (Long userId : whitelist) {
            if (userId == null) continue;

            Member m = guild.getMemberById(userId);
            if (m != null) {
                getChannel().upsertPermissionOverride(m)
                        .setAllowed(EnumSet.of(Permission.VOICE_CONNECT))
                        .queue();

                getChannel().upsertPermissionOverride(m)
                        .setAllowed(Permission.VIEW_CHANNEL)
                        .queue();
            }
        }

        boolean invisible = map.getCastOrDefault("visible", Boolean.class, false);
        boolean blocked = map.getCastOrDefault("blocked", Boolean.class, false);

        EnumSet<Permission> allow = EnumSet.noneOf(Permission.class);
        EnumSet<Permission> deny = EnumSet.noneOf(Permission.class);

        if (!invisible) allow.add(Permission.VIEW_CHANNEL);
        else deny.add(Permission.VIEW_CHANNEL);

        if (!blocked) allow.add(Permission.VOICE_CONNECT);
        else deny.add(Permission.VOICE_CONNECT);

        getChannel().upsertPermissionOverride(guild.getPublicRole())
                .setAllowed(allow.isEmpty() ? null : allow)
                .setDenied(deny.isEmpty() ? null : deny)
                .queue();
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

    public TypeMap getSettings() {
        return settings;
    }

}
