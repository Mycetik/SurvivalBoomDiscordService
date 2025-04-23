package net.survivalboom.sbds.modules.privatevoice;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserRepositoryHandler;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VoiceManager extends Manager implements Listener {

    private static final Logger log = LoggerFactory.getLogger(VoiceManager.class);
    private final PrivateVoiceModule module;

    private final Set<PrivateVoice> voiceSet = new HashSet<>();

    private final Map<Long, Long> channelCreators = new HashMap<>();

    private IGuildRepositoryHandler guildRepositoryHandler;
    private IUserRepositoryHandler userRepositoryHandler;


    public VoiceManager(PrivateVoiceModule module) {
        this.module = module;

    }

    @Override
    protected void init0() {
        guildRepositoryHandler = module.getSbds().getDatabase().getRepositoryHandler("sbds:guilds", IGuildRepositoryHandler.class);
        userRepositoryHandler = module.getSbds().getDatabase().getRepositoryHandler("sbds:users", IUserRepositoryHandler.class);
    }

    @Override
    protected void shutdown0() {
        guildRepositoryHandler = null;
    }

    public void setup(VoiceChannel channel) {

        channelCreators.remove(channel.getGuild().getIdLong());
        channelCreators.put(channel.getGuild().getIdLong(), channel.getIdLong());

        IGuildData data = guildRepositoryHandler.createGuildData(channel.getGuild().getIdLong());

        TypeMap map = data.container().getOrCreate(NamespacedKey.fromModule(module.getModule(), "voice_creators"));
        map.put("channel", channel.getIdLong());

        data.save();

    }

    @EventHandler
    public void onVoice(GuildVoiceUpdateEvent voice) {
        long guildId = voice.getGuild().getIdLong();

        if (!channelCreators.containsKey(guildId)) {
            IGuildData data = guildRepositoryHandler.getGuildData(guildId);
            if (data == null) return;

            TypeMap map = data.container().get(NamespacedKey.fromModule(module.getModule(), "voice_creators"));
            if (map == null) return;
            Long channelId = map.getCastOrNull("channel", Long.class);
            if (channelId == null) return;

            channelCreators.put(guildId, channelId);
        }

        Member member = voice.getMember();
        AudioChannel joined = voice.getChannelJoined();
        AudioChannel left = voice.getChannelLeft();

        // АХТУНГ Я ЛОХ И ЭТО СДЕЛАЛ ЧАТ ГПТ
        if (left instanceof VoiceChannel leftChannel) {
            PrivateVoice voiceChannel = voiceSet.stream()
                    .filter(v -> v.getChannel().getIdLong() == leftChannel.getIdLong())
                    .findFirst()
                    .orElse(null);

            if (voiceChannel != null && member.equals(voiceChannel.getOwner())) {
                log.info("Deleting private channel {} owned by {}", leftChannel.getName(), member.getEffectiveName());

                voiceSet.remove(voiceChannel);
                leftChannel.delete().queue();
            }
        }

        if (joined == null || joined.getIdLong() != channelCreators.get(guildId)) return;
        if (!(joined instanceof VoiceChannel creatorChannel)) return;

        log.info("Voice joined creator channel: {}", creatorChannel.getIdLong());

        PrivateVoice existing = getVoiceChannel(member);
        if (existing != null) {
            voice.getGuild().moveVoiceMember(member, existing.getChannel()).queue();
        } else {
            createVoice(creatorChannel, member);
        }
    }

    public PrivateVoice getVoiceChannel(Member member) {
        return voiceSet.stream().filter(v -> v.getOwner().equals(member)).findFirst().orElse(null);
    }

    public void createVoice(VoiceChannel channel, Member member) {

        Category category = channel.getParentCategory();

        IUserData userData = userRepositoryHandler.createUser(member.getUser());
        TypeMap settingsMap = userData.container().getOrCreate(NamespacedKey.fromModule(module.getModule(), "voice_settings"));

        String name = (String) settingsMap.getOrDefault("name", member.getUser().getName());
        Guild guild = channel.getGuild();
        VoiceChannel tempVoice = guild.createVoiceChannel(name, category).complete();

        guild.moveVoiceMember(member, tempVoice).complete();

        PrivateVoice privateVoice = new PrivateVoice(tempVoice);
        privateVoice.setOwner(member);

        voiceSet.add(privateVoice);
    }
}
