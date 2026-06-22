package net.survivalboom.sbds.modules.voice.voice;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.survivalboom.sbds.api.interaction.component.ComponentInteractionInfo;
import net.survivalboom.sbds.api.interaction.component.ComponentInteractionRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class ControlPanelListener {

    private final VoiceManager voiceManager;

    public ControlPanelListener(@NotNull VoiceManager voiceManager) {
        this.voiceManager = voiceManager;
    }

    public void onControlPanelDropdown(@NotNull ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        String value = info.event().getValues().getFirst();
        switch (value) {

            case "change-name" -> changeName(info);

            case "set-limit" -> setLimit(info);

            case "lock" -> lock(info);

            case "hide" -> hide(info);

            case "blacklist" -> blacklist(info);

            case "whitelist" -> whitelist(info);

            case "mute" -> mute(info);

            case "delete" -> delete(info);

            default -> throw new IllegalArgumentException("Invalid `" + value + "` value");

        }

    }

    private void changeName(ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.replyModal("privatevoicemodule:rename").onSuccess(modal -> {

                String name = modal.field("name").orElseThrow().getAsString();

                voice.setChannelName(name);

                modal.reply("voice.control.rename.success")
                        .withPlaceholders("name", name)
                        .setEphemeral(true)
                        .queue();

            }).withTimeout(30000).queue();

        }, info);

    }

    private void setLimit(ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.replyModal("privatevoicemodule:limit").onSuccess(modal -> {

                String limitRaw = modal.field("limit").orElseThrow().getAsString();

                int limit;
                try {
                    limit = Integer.parseInt(limitRaw);
                }

                catch (NumberFormatException e) {
                    modal.reply("voice.control.set-limit.invalid")
                            .withPlaceholders("input", limitRaw)
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                voice.setMaxMembers(limit);

                modal.reply("voice.control.set-limit.success")
                        .withPlaceholders("limit", limit)
                        .setEphemeral(true)
                        .queue();

                voice.updateControlPanel(false);

            }).withTimeout(30000).queue();

        }, info);

    }

    private void lock(ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        onVoice(voice -> {

            boolean state = !voice.isLocked();

            voice.setLocked(state);

            String str = state ? "voice.control.lock.locked" : "voice.control.lock.unlocked";
            info.reply(str).setEphemeral(true).queue();

        }, info);

    }

    private void hide(ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        onVoice(voice -> {

            boolean state = !voice.isHidden();

            voice.setHidden(state);

            String str = state ? "voice.control.hide.hidden" : "voice.control.hide.show";
            info.reply(str).setEphemeral(true).queue();

        }, info);

    }

    private void blacklist(ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.reply("voice.control.blacklist.select")
                .withComponents(builder -> builder.addEntityDropdown("member", ComponentInteractionRequest.ExpireMode.ALL, dropdown -> {

                    Member member = dropdown.event().getMentions().getMembers().getFirst();
                    if (member.getUser().isBot()) {
                        dropdown.reply("voice.control.blacklist.bot").setEphemeral(true).queue();
                        return;
                    }

                    if (voice.getOwner().equals(member)) {
                        dropdown.reply("voice.control.blacklist.self").setEphemeral(true).queue();
                        return;
                    }

                    boolean state = !voice.getBlackList().contains(member);

                    voice.blacklist(member, state);

                    String str = state ? "voice.control.blacklist.added" : "voice.control.blacklist.removed";

                    voice.updateControlPanel(false);

                    dropdown.reply(str)
                            .withPlaceholders("member", member)
                            .setEphemeral(true)
                            .queue();

                }))
                .setEphemeral(true)
                .queue();

        }, info);

    }

    private void whitelist(ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.reply("voice.control.whitelist.select")
                .withComponents(builder -> builder.addEntityDropdown("member", ComponentInteractionRequest.ExpireMode.ALL, dropdown -> {

                    Member member = dropdown.event().getMentions().getMembers().getFirst();
                    if (member.getUser().isBot()) {
                        dropdown.reply("voice.control.blacklist.bot").setEphemeral(true).queue();
                        return;
                    }

                    if (voice.getOwner().equals(member)) {
                        dropdown.reply("voice.control.whitelist.self").setEphemeral(true).queue();
                        return;
                    }

                    boolean state = !voice.getBlackList().contains(member);

                    voice.whitelist(member, state);

                    String str = state ? "voice.control.whitelist.added" : "voice.control.whitelist.removed";
                    voice.updateControlPanel(false);

                    dropdown.reply(str)
                            .withPlaceholders("member", member)
                            .setEphemeral(true)
                            .queue();

                }))
                .setEphemeral(true)
                .queue();

        }, info);

    }

    private void mute(ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.reply("voice.control.mute.select").withComponents(builder -> builder.addEntityDropdown("member", ComponentInteractionRequest.ExpireMode.ALL, dropdown -> {

                Member member = dropdown.event().getMentions().getMembers().getFirst();
                if (member.getUser().isBot()) {
                    dropdown.reply("voice.control.mute.bot").setEphemeral(true).queue();
                    return;
                }

                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState == null || !voice.getChannel().equals(voiceState.getChannel())) {
                    dropdown.reply("voice.control.mute.not-in-voice").setEphemeral(true).queue();
                    return;
                }

                if (voice.getOwner().equals(member)) {
                    dropdown.reply("voice.control.mute.self").setEphemeral(true).queue();
                    return;
                }

                boolean state = !voice.getMuted().contains(member);

                voice.setMuted(member, state);

                String str = state ? "voice.control.mute.muted" : "voice.control.mute.unmuted";
                voice.updateControlPanel(false);

                dropdown.reply(str)
                        .withPlaceholders("member", member.getAsMention())
                        .setEphemeral(true)
                        .queue();

            }))
            .setEphemeral(true)
            .queue();

        }, info);

    }

    private void delete(ComponentInteractionInfo<StringSelectInteractionEvent> info) {
        onVoice(voiceManager::deleteVoice, info);
    }

    private void onVoice(Consumer<PrivateVoice> consumer, ComponentInteractionInfo<StringSelectInteractionEvent> info) {

        Member member = Objects.requireNonNull(info.member());

        GuildVoiceState voiceState = Objects.requireNonNull(member).getVoiceState();
        if (voiceState == null || !(voiceState.getChannel() instanceof VoiceChannel channel)) {
            info.reply("voice.control.not-in-voice").setEphemeral(true).queue();
            return;
        }

        PrivateVoice voice = voiceManager.getVoice(channel);
        if (voice == null) {
            info.reply("voice.control.not-in-voice").setEphemeral(true).queue();
            return;
        }

        if (!voice.getOwner().equals(member) && !voiceManager.getModule().getSbds().getPermissionManager().hasPermission(member, "voice.admin", false)){
            info.reply("voice.control.not-owner").setEphemeral(true).queue();
            voice.updateControlPanel(false);
            return;
        }

        consumer.accept(voice);

    }

}
