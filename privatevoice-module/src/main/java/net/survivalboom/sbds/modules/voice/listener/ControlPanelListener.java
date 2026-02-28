package net.survivalboom.sbds.modules.voice.listener;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.survivalboom.sbds.api.interaction.dropdown.string.StringDropdownInteractionInfo;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import net.survivalboom.sbds.api.interaction.modal.ModalTemplate;
import net.survivalboom.sbds.modules.voice.voice.PrivateVoice;
import net.survivalboom.sbds.modules.voice.voice.VoiceManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class ControlPanelListener {

    private final VoiceManager voiceManager;


    public ControlPanelListener(@NotNull VoiceManager voiceManager) {
        this.voiceManager = voiceManager;

        ModalTemplate renameModal = ModalTemplate.builder()
                .setTitle("[voice.control.rename.modal.title]")
                .addInput("name", "[voice.control.rename.modal.input-name]", "[voice.control.rename.modal.input-placeholder]", TextInputStyle.SHORT, 3, 20, true)
                .build();

        ModalTemplate limitModal = ModalTemplate.builder()
                .setTitle("[voice.control.set-limit.modal.title]")
                .addInput("limit", "[voice.control.set-limit.modal.input-name]", "[voice.control.set-limit.modal.input-placeholder]", TextInputStyle.SHORT, 1, 2, true)
                .build();

        IModalInteractionManager modalManager = voiceManager.getModule().getSbds().getModalInteractionManager();
        modalManager.registerModal(voiceManager.getModule(), "rename", renameModal);
        modalManager.registerModal(voiceManager.getModule(), "limit", limitModal);

    }

    public void onControlPanelDropdown(@NotNull StringDropdownInteractionInfo info) {

        String value = info.getSelectedOptions().getFirst().getValue();
        switch (value) {

            case "change-name" -> changeName(info);

            case "set-limit" -> setLimit(info);

            case "lock" -> lock(info);

            case "hide" -> hide(info);

            case "blacklist" -> blacklist(info);

            case "whitelist" -> whitelist(info);

            case "mute" -> mute(info);

            case "delete" -> delete(info);

            default -> info.replyRaw("Invalid value `" + value + "`.").setEphemeral(true).queue();

        }

    }


    private void changeName(StringDropdownInteractionInfo info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.replyModal("privatevoicemodule:rename").onSuccess(modal -> {

                String name = Objects.requireNonNull(modal.value("name"));

                voice.setChannelName(name);
                modal.reply("voice.control.rename.success")
                        .withPlaceholders("{NAME}", name)
                        .send()
                        .setEphemeral(true)
                        .queue();

            }).withTimeout(30000).queue();

        }, info);

    }

    private void setLimit(StringDropdownInteractionInfo info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.replyModal("privatevoicemodule:limit").onSuccess(modal -> {

                String limitRaw = Objects.requireNonNull(modal.value("limit"));
                int limit;

                try {
                    limit = Integer.parseInt(limitRaw);
                }

                catch (NumberFormatException e) {
                    modal.reply("voice.control.set-limit.invalid")
                            .withPlaceholders("{INPUT}")
                            .send()
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                voice.setMaxMembers(limit);

                modal.reply("voice.control.set-limit.success")
                        .withPlaceholders("{LIMIT}", limit)
                        .send()
                        .setEphemeral(true)
                        .queue();

                voice.updateControlPanel(false);

            }).withTimeout(30000).queue();

        }, info);

    }

    private void lock(StringDropdownInteractionInfo info) {

        onVoice(voice -> {

            boolean state = !voice.isLocked();

            voice.setLocked(state);

            String str = state ? "voice.control.lock.locked" : "voice.control.lock.unlocked";
            info.reply(str).send().setEphemeral(true).queue();

        }, info);

    }

    private void hide(StringDropdownInteractionInfo info) {

        onVoice(voice -> {

            boolean state = !voice.isHidden();

            voice.setHidden(state);

            String str = state ? "voice.control.hide.hidden" : "voice.control.hide.show";
            info.reply(str).send().setEphemeral(true).queue();

        }, info);

    }

    private void blacklist(StringDropdownInteractionInfo info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.reply("voice.control.blacklist.select").entityDropdownCallback("member", dropdown -> {

                Member member = dropdown.mentions().getMembers().getFirst();
                if (member.getUser().isBot()) {
                    dropdown.reply("voice.control.blacklist.bot").send().setEphemeral(true).queue();
                    return;
                }

                if (voice.getOwner().equals(member)) {
                    dropdown.reply("voice.control.blacklist.self").send().setEphemeral(true).queue();
                    return;
                }

                boolean state = !voice.getBlackList().contains(member);

                voice.blacklist(member, state);

                String str = state ? "voice.control.blacklist.added" : "voice.control.blacklist.removed";

                dropdown.reply(str)
                        .withPlaceholders("{MEMBER}", member.getAsMention())
                        .send()
                        .setEphemeral(true)
                        .queue(v -> voice.updateControlPanel(false));


            }, null, 30000).send().setEphemeral(true).queue();

        }, info);

    }

    private void whitelist(StringDropdownInteractionInfo info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.reply("voice.control.whitelist.select").entityDropdownCallback("member", dropdown -> {

                Member member = dropdown.mentions().getMembers().getFirst();
                if (member.getUser().isBot()) {
                    dropdown.reply("voice.control.blacklist.bot").send().setEphemeral(true).queue();
                    return;
                }

                if (voice.getOwner().equals(member)) {
                    dropdown.reply("voice.control.whitelist.self").send().setEphemeral(true).queue();
                    return;
                }


                boolean state = !voice.getBlackList().contains(member);

                voice.whitelist(member, state);

                String str = state ? "voice.control.whitelist.added" : "voice.control.whitelist.removed";

                dropdown.reply(str)
                        .withPlaceholders("{MEMBER}", member.getAsMention())
                        .send()
                        .setEphemeral(true)
                        .queue(v -> voice.updateControlPanel(false));


            }, null, 30000).send().setEphemeral(true).queue();

        }, info);

    }

    public void mute(StringDropdownInteractionInfo info) {

        onVoice(voice -> {

            voice.updateControlPanel(false);

            info.reply("voice.control.mute.select").entityDropdownCallback("member", dropdown -> {

                Member member = dropdown.mentions().getMembers().getFirst();
                if (member.getUser().isBot()) {
                    dropdown.reply("voice.control.mute.bot").send().setEphemeral(true).queue();
                    return;
                }

                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState == null || !voice.getChannel().equals(voiceState.getChannel())) {
                    dropdown.reply("voice.control.mute.not-in-voice").send().setEphemeral(true).queue();
                    return;
                }

                if (voice.getOwner().equals(member)) {
                    dropdown.reply("voice.control.mute.self").send().setEphemeral(true).queue();
                    return;
                }

                boolean state = !voice.getMuted().contains(member);

                voice.setMuted(member, state);

                String str = state ? "voice.control.mute.muted" : "voice.control.mute.unmuted";

                dropdown.reply(str)
                        .withPlaceholders("{MEMBER}", member.getAsMention())
                        .send()
                        .setEphemeral(true)
                        .queue(v -> voice.updateControlPanel(false));

            }, null, 30000).send().setEphemeral(true).queue();

        }, info);

    }



    private void delete(StringDropdownInteractionInfo info) {
        onVoice(voiceManager::deleteVoice, info);
    }


    private void onVoice(Consumer<PrivateVoice> consumer, StringDropdownInteractionInfo info) {

        Member member = Objects.requireNonNull(info.member());

        GuildVoiceState voiceState = Objects.requireNonNull(member).getVoiceState();
        if (voiceState == null || !(voiceState.getChannel() instanceof VoiceChannel channel)) {
            info.reply("voice.control.not-in-voice").send().setEphemeral(true).queue();
            return;
        }

        PrivateVoice voice = voiceManager.findByChannel(channel);
        if (voice == null) {
            info.reply("voice.control.not-in-voice").send().setEphemeral(true).queue();
            return;
        }

        if (!voice.getOwner().equals(member) && !voiceManager.getModule().getSbds().getPermissionManager().hasPermission(member, "voice.admin", false)){
            info.reply("voice.control.not-owner").send().setEphemeral(true).queue();
            voice.updateControlPanel(false);
            return;
        }

        consumer.accept(voice);

    }


}
