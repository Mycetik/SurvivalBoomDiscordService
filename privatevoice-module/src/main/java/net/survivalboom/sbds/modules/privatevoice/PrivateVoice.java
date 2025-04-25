package net.survivalboom.sbds.modules.privatevoice;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class PrivateVoice {
    private static final Logger log = LoggerFactory.getLogger(PrivateVoice.class);
    private final VoiceChannel channel;
    private Member owner;

    private TypeMap settings;
    private IUserData userData;

    public PrivateVoice(VoiceChannel channel) {
        this.channel = channel;
    }

    public void setup(TypeMap map) {
        VoiceChannelManager manager = getChannel().getManager();
        settings = map;

        int max = map.getCastOrDefault("max", Integer.class, 20);
        manager.setUserLimit(max).queue();

        List<Long> blacklist = ((List<?>) map.getOrDefault("blacklist", Collections.emptyList()))
                .stream()
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
        Guild guild = getChannel().getGuild();
        log.info(String.valueOf(blacklist));

        for (Long userId : blacklist) {
            if (userId == null) continue;

            Member member = guild.getMemberById(userId);
            log.info(String.valueOf(member.getIdLong()));
            if (member != null) {
                getChannel().upsertPermissionOverride(member)
                        .deny(Permission.VOICE_CONNECT)
                        .queue();
            }
        }

        List<Long> whitelist = ((List<?>) map.getOrDefault("whitelist", Collections.emptyList()))
                .stream()
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
        log.info(String.valueOf(whitelist));
        for (Long userId : whitelist) {
            if (userId == null) continue;

            Member m = guild.getMemberById(userId);
            log.info(String.valueOf(m.getIdLong()));
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

        userData.save();
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

    public void setUserData(IUserData userData) {
        this.userData = userData;
    }

    public IUserData getUserData() {
        return userData;
    }
}
