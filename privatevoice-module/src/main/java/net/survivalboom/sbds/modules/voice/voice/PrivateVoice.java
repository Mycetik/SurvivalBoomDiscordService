package net.survivalboom.sbds.modules.voice.voice;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PrivateVoice {

    private final VoiceManager voiceManager;

    private final VoiceChannel channel;


    private Member owner;

    private final Set<Member> blackList = new HashSet<>();

    private final Set<Member> whitelist = new HashSet<>();

    private final Set<Member> muted = new HashSet<>();

    private boolean locked = false;

    private boolean hidden = false;


    private Message controlPanelMessage = null;


    public PrivateVoice(@NotNull VoiceManager voiceManager, @NotNull VoiceChannel channel) {
        this.channel = channel;
        this.voiceManager = voiceManager;
    }

    public @NotNull VoiceManager getVoiceManager() {
        return voiceManager;
    }

    //
    // TICK
    //

    public void tick() {

        boolean exists = channel.getGuild().getChannelById(VoiceChannel.class, channel.getId()) != null;
        if (!exists) {
            voiceManager.deleteVoice(this);
            return;
        }

        List<Member> members = channel.getMembers();
        if (members.isEmpty() || members.stream().allMatch(m -> m.getUser().isBot())) {
            voiceManager.deleteVoice(this);
            return;
        }

        kickMembers(members);

    }

    private void kickMembers(List<Member> members) {

        List<Member> toKick = members.stream()
                .filter(m -> !m.getUser().isBot())
                .filter(m -> !voiceManager.getModule().getSbds().getPermissionManager().hasPermission(m, "voice.admin", false))
                .filter(m -> !owner.equals(m))
                .filter(m -> blackList.contains(m) || (isLocked() && !whitelist.contains(m)))
                .toList();

        toKick.forEach(m -> m.getGuild().kickVoiceMember(m).queue());

    }

    //
    // FUNCTIONS
    //

    public void setOwner(@NotNull Member member) {

        Member lastOwner = owner;
        if (lastOwner != null) {
            Objects.requireNonNull(channel.getPermissionOverride(lastOwner)).delete().queue();
        }

        blacklist(member, false);
        whitelist(member, false);

        this.owner = member;

        EnumSet<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT);
        channel.getManager().putPermissionOverride(member, permissions, null).queue();

    }

    public void setLocked(boolean v) {

        if (locked == v) {
            return;
        }

        if (hidden && !v) {
            setHidden(false);
        }

        this.locked = v;

        Role role = channel.getGuild().getPublicRole();

        if (v) {
            EnumSet<Permission> denyPermissions = EnumSet.of(Permission.VOICE_CONNECT);
            channel.getManager().putPermissionOverride(role, null, denyPermissions).queue();
            kickMembers(channel.getMembers());
        }

        else {
            EnumSet<Permission> allowPermissions = EnumSet.of(Permission.VOICE_CONNECT);
            channel.getManager().putPermissionOverride(role, allowPermissions, null).queue();
        }

        updateControlPanel(false);

    }

    public void setHidden(boolean v) {

        if (hidden == v) {
            return;
        }

        if (!locked && v) {
            setLocked(true);
        }

        this.hidden = v;

        Role role = channel.getGuild().getPublicRole();

        if (v) {
            EnumSet<Permission> denyPermissions = EnumSet.of(Permission.VIEW_CHANNEL);
            channel.getManager().putPermissionOverride(role, null, denyPermissions).queue();
        }

        else {
            EnumSet<Permission> allowPermissions = EnumSet.of(Permission.VIEW_CHANNEL);
            channel.getManager().putPermissionOverride(role, allowPermissions, null).queue();
        }

        updateControlPanel(false);

    }

    public void setMuted(@NotNull Member member, boolean v) {

        if (muted.contains(member) == v) {
            return;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !channel.equals(voiceState.getChannel())) {
            throw new IllegalStateException("Member not in the current voice channel");
        }

        Guild guild = channel.getGuild();
        VoiceChannel vChannel = voiceManager.getVoiceCreator(guild).join();
        if (vChannel == null) return;

        EnumSet<Permission> permissions = EnumSet.of(Permission.VOICE_SPEAK);
        if (v) {
            channel.getManager().putPermissionOverride(member, null, permissions).queue(vo -> reMute(guild, member, vChannel));
            muted.add(member);
        }

        else {
            channel.getManager().putPermissionOverride(member, permissions, null).queue(vo -> reMute(guild, member, vChannel));
            muted.remove(member);
        }

    }

    private void reMute(Guild guild, Member member, VoiceChannel vChannel) {
        voiceManager.ignoredMembers.add(member);
        guild.moveVoiceMember(member, vChannel)
                .queue(v -> guild.moveVoiceMember(member, channel)
                        .queue(vv -> voiceManager.ignoredMembers.remove(member))
                );
    }

    public void setMaxMembers(int limit) {
        if (limit > 50) limit = 0;
        channel.getManager().setUserLimit(limit).queue();
    }

    public void setChannelName(@NotNull String name) {
        channel.getManager().setName(name).queue();
    }


    public void whitelist(@NotNull Member member, boolean value) {

        if (value) {
            blacklist(member, false);
        }

        if (whitelist.contains(member) == value) {
            return;
        }


        if (value) {

            whitelist.add(member);

            EnumSet<Permission> permissions = EnumSet.of(Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL);
            channel.getManager().putMemberPermissionOverride(member.getIdLong(), permissions, null).queue();

        }

        else {
            whitelist.remove(member);
            Objects.requireNonNull(channel.getPermissionOverride(member)).delete().queue();
        }



    }

    public void blacklist(@NotNull Member member, boolean value) {

        if (value) {
            whitelist(member, false);
        }

        if (blackList.contains(member) == value) {
            return;
        }

        if (value) {

            blackList.add(member);

            EnumSet<Permission> permissions = EnumSet.of(Permission.VOICE_CONNECT);
            channel.getManager().putMemberPermissionOverride(member.getIdLong(), null, permissions).queue();

        }

        else {
            blackList.remove(member);
            Objects.requireNonNull(channel.getPermissionOverride(member)).delete().queue();
        }

    }

    //
    // PANEL
    //

    public void updateControlPanel(boolean create) {

        IMessages messages = voiceManager.getModule().getSbds().getMessages();

        String whitelistString = whitelist.isEmpty() ? "$[values.none]" : String.join(", ", whitelist.stream().map(m -> m.getUser().getEffectiveName()).toList());
        String blacklistString = blackList.isEmpty() ? "$[values.none]" : String.join(", ", blackList.stream().map(m -> m.getUser().getEffectiveName()).toList());
        String mutedString = muted.isEmpty() ? "$[values.none]" : String.join(", ", muted.stream().map(m -> m.getUser().getEffectiveName()).toList());

        Placeholders placeholders = new Placeholders();
        placeholders
                .add("channel", channel)
                .add("owner", owner)
                .add("locked", value(locked))
                .add("hidden", value(hidden))
                .add("whitelist", whitelistString)
                .add("blacklist", blacklistString)
                .add("muted", mutedString);

        boolean newMsg = controlPanelMessage == null || create;

        var builder = messages.createMessageBuilder("voice.control.panel", owner.getUser())
                .withPlaceholders(placeholders);

        if (newMsg) {
            channel.sendMessage(builder.build().build()).queue(m -> controlPanelMessage = m);
        }

        else {
            controlPanelMessage.editMessage(MessageEditData.fromCreateData(builder.build().build())).queue();
        }

    }

    //
    // GETTERS
    //

    public @NotNull VoiceChannel getChannel() {
        return channel;
    }

    public @NotNull Guild getGuild() {
        return channel.getGuild();
    }

    public @NotNull Member getOwner() {
        return owner;
    }

    public @NotNull List<Member> getMembers() {
        return channel.getMembers();
    }


    public @NotNull Set<Member> getBlackList() {
        return new HashSet<>(blackList);
    }

    public @NotNull Set<Member> getWhitelist() {
        return new HashSet<>(whitelist);
    }

    public @NotNull Set<Member> getMuted() {
        return new HashSet<>(muted);
    }


    public boolean isHidden() {
        return hidden;
    }

    public boolean isLocked() {
        return locked;
    }

    // // //

    private String value(boolean v) {
        return v ? "$[values.true]" : "$[values.false]";
    }

    @Override
    public String toString() {
        return String.format("PrivateVoice{name=%s, guild=%s, owner=%s}", channel.getName(), channel.getGuild().getName(), owner.getEffectiveName());
    }

}
