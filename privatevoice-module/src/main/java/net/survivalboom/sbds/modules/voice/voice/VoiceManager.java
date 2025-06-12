package net.survivalboom.sbds.modules.voice.voice;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.scheduler.IScheduler;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.Manager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VoiceManager extends Manager implements Listener {

    private static final Logger log = LoggerFactory.getLogger(VoiceManager.class);

    private final Set<PrivateVoice> voiceSet = new HashSet<>();

    private final ModuleMain module;

    private final IScheduler scheduler;


    private ISchedulerTask task;


    public VoiceManager(@NotNull ModuleMain module) {
        this.scheduler = module.getSbds().getScheduler();
        this.module = module;
    }


    @Override
    protected void init0() {
        task = scheduler.schedule(module, this::task, 10, 3000);
    }

    @Override
    protected void shutdown0() {

        task.cancelAndWait(5000, true);

        task = null;

        getVoices().forEach(this::deleteVoice);
        voiceSet.clear();

    }


    public @NotNull PrivateVoice createVoice(@NotNull Member member, @NotNull VoiceChannel creator) {

        VoiceChannel channel = createChannel(creator, member.getEffectiveName());
        member.getGuild().moveVoiceMember(member, channel).queue();

        PrivateVoice privateVoice = new PrivateVoice(this, channel);
        privateVoice.setOwner(member);

        voiceSet.add(privateVoice);

        privateVoice.updateControlPanel(true);

        return privateVoice;

    }

    private VoiceChannel createChannel(VoiceChannel creator, String name) {

        Category category = creator.getParentCategory();
        if (category == null) return creator.getGuild().createVoiceChannel(name).complete();

        return category.createVoiceChannel(name).complete();

    }

    public void deleteVoice(@NotNull PrivateVoice voice) {
        voiceSet.remove(voice);
        voice.getChannel().delete().queue();
    }

    public @Nullable PrivateVoice findByChannel(@NotNull VoiceChannel channel) {
        return voiceSet.stream().filter(v -> v.getChannel().equals(channel)).findAny().orElse(null);
    }


    public @NotNull ModuleMain getModule() {
        return module;
    }

    public @NotNull List<PrivateVoice> getVoices() {
        return new ArrayList<>(voiceSet);
    }

    private void task() {

        for (PrivateVoice voice : voiceSet) {

            try {

                voice.tick();

            }

            catch (Throwable t) {
                log.error("Failed to tick channel `{}` in guild `{}`.", voice.getChannel().getName(), voice.getChannel().getGuild().getName(), t);
            }

        }

    }

}
