package net.survivalboom.sbds.modules.privatevoice;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserRepositoryHandler;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;

public class VoiceManager extends Manager implements Listener {

    private static final Logger log = LoggerFactory.getLogger(VoiceManager.class);
    private final PrivateVoiceModule module;

    private final Set<PrivateVoice> voiceSet = new HashSet<>();

    private final Map<Long, Long> channelCreators = new HashMap<>();

    private IGuildRepositoryHandler guildRepositoryHandler;
    private IUserRepositoryHandler userRepositoryHandler;
    private IMessages messages;


    public VoiceManager(PrivateVoiceModule module) {
        this.module = module;

    }

    @Override
    protected void init0() {
        guildRepositoryHandler = module.getSbds().getDatabase().getRepositoryHandler("sbds:guilds", IGuildRepositoryHandler.class);
        userRepositoryHandler = module.getSbds().getDatabase().getRepositoryHandler("sbds:users", IUserRepositoryHandler.class);
        messages = module.getSbds().getMessages();
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

            if (voiceChannel != null && voiceChannel.getChannel().getMembers().isEmpty()) {
                log.info("Deleting private channel {} owned by {}", leftChannel.getName(), member.getEffectiveName());
                IUserData userData = userRepositoryHandler.createUser(member.getUser());
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
        privateVoice.setUserData(userData);

        voiceSet.add(privateVoice);
        privateVoice.setup(settingsMap);
        sendPanel(privateVoice);
    }

    @EventHandler
    public void onVoiceDeleted(ChannelDeleteEvent event) {
        if (!(event.getChannel() instanceof VoiceChannel channel)) return;

        voiceSet.removeIf(v -> v.getChannel() == channel);
    }

    @EventHandler
    public void onVoiceTextMessage(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        Channel channel = event.getChannel();

        Member member = event.getMember();
        if (member == null) return;

        PrivateVoice voice = getVoiceChannel(member);
        if (voice == null || voice.getChannel().getIdLong() != channel.getIdLong()) return;

        if (event.getMessage().getContentRaw().equals("+")) {
            event.getMessage().delete().queue();
            sendPanel(voice);
        }
    }



    private void sendPanel(PrivateVoice voice) {
        VoiceChannel channel = voice.getChannel();
        Placeholders placeholders = new Placeholders();
        placeholders.add("{CHANNEL}", channel.getName());
        placeholders.add("{OWNER}", voice.getOwner());
        placeholders.add("{MAX}", voice.getSettings().getCastOrDefault("max", Integer.class, 20));
        placeholders.add("{VISIBLE}", voice.getSettings().getCastOrDefault("visible", Boolean.class, false) ? "Виден" : "Скрыт");
        placeholders.add("{BLOCKED}", voice.getSettings().getCastOrDefault("blocked", Boolean.class, false) ? "Заблокирован" : "Разблокирован");

        boolean isBlocked = voice.getSettings().getCastOrDefault("blocked", Boolean.class, false);
        boolean isVisible = voice.getSettings().getCastOrDefault("visible", Boolean.class, false);

        String lockOption = isBlocked ? "voice.unlock" : "voice.lock";
        String visibilityOption = isVisible ? "voice.hide" : "voice.show";

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("voice:settings");
        menuBuilder.addOption(String.valueOf(Objects.requireNonNull(messages.getMessage("voice.rename", voice.getOwner().getUser(), true)).text()), "rename");
        menuBuilder.setPlaceholder(String.valueOf(Objects.requireNonNull(messages.getMessage("voice.placeholder", voice.getOwner().getUser(), true)).text()));
        menuBuilder.addOption(String.valueOf(Objects.requireNonNull(messages.getMessage(lockOption, voice.getOwner().getUser(), true)).text()), isBlocked ? "unlock" : "lock");
        menuBuilder.addOption(String.valueOf(Objects.requireNonNull(messages.getMessage(visibilityOption, voice.getOwner().getUser(), true)).text()), isVisible ? "hide" : "show");
        menuBuilder.addOption(String.valueOf(Objects.requireNonNull(messages.getMessage("voice.set_limit", voice.getOwner().getUser(), true)).text()), "set_limit");
        menuBuilder.addOption(String.valueOf(Objects.requireNonNull(messages.getMessage("voice.delete", voice.getOwner().getUser(), true)).text()), "delete");

        StringSelectMenu.Builder lists = StringSelectMenu.create("voice:lists")
                        .setPlaceholder(Objects.requireNonNull(messages.getMessage("voice.placeholder", voice.getOwner().getUser(), true)).text())
                        .addOption(String.valueOf(Objects.requireNonNull(messages.getMessage("voice.blacklist", voice.getOwner().getUser(), true)).text()), "blacklist")
                        .addOption(String.valueOf(Objects.requireNonNull(messages.getMessage("voice.whitelist", voice.getOwner().getUser(), true)).text()), "whitelist");

        channel.getHistory().getRetrievedHistory().forEach(history -> {
            history.delete().queue();
        });

        messages.sendMessage(channel, placeholders, "voice.panel", voice.getOwner().getUser())
                .addActionRow(menuBuilder.build())
                .addActionRow(lists.build())
                .queue();
    }

    @EventHandler
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();
        Member member = event.getMember();
        if (member == null || member.getUser().isBot()) return;

        if (componentId.equals("voice:lists")) {
            String selected = event.getValues().get(0);
            PrivateVoice voice = getVoiceChannel(member);
            if (voice == null) {
                return;
            }

            VoiceChannel vc = voice.getChannel();
            switch (selected) {
                case "blacklist", "whitelist" -> {

                    List<Member> guildMembers = vc.getGuild().getMembers();


                    EntitySelectMenu menu = EntitySelectMenu.create("voice:" + selected + "_select", EntitySelectMenu.SelectTarget.USER)
                            .setPlaceholder("Выбери участников")
                            .build();

                    event.reply("Выбери участников для " + (selected.equals("blacklist") ? "чёрного" : "белого") + " списка:")
                            .addActionRow(menu)
                            .setEphemeral(true)
                            .queue();
                }
            }
        }

        if (!componentId.equals("voice:settings")) return;

        String selected = event.getValues().get(0);
        PrivateVoice voice = getVoiceChannel(member);
        if (voice == null) {
            return;
        }

        TypeMap settings = voice.getSettings();
        VoiceChannel vc = voice.getChannel();

        switch (selected) {
            case "rename" -> {
                Modal modal = Modal.create("voice:rename", "Переименование канала")
                        .addActionRow(TextInput.create("name", "Новое имя", TextInputStyle.SHORT)
                                .setPlaceholder("Новая комната")
                                .setRequired(true)
                                .build())
                        .build();
                event.replyModal(modal).queue();
            }
            case "lock" -> {
                settings.put("blocked", true);
                event.reply("🔒 Канал заблокирован").setEphemeral(true).queue();
            }
            case "unlock" -> {
                settings.put("blocked", false);
                event.reply("🔓 Канал разблокирован").setEphemeral(true).queue();
            }
            case "hide" -> {
                settings.put("visible", true);
                event.reply("👁️ Канал скрыт").setEphemeral(true).queue();
            }
            case "show" -> {
                settings.put("visible", false);
                event.reply("👁️ Канал показан").setEphemeral(true).queue();
            }
            case "set_limit" -> {
                Modal modal = Modal.create("voice:set_limit", "Установить лимит участников")
                        .addActionRow(TextInput.create("limit", "Новый лимит", TextInputStyle.SHORT)
                                .setPlaceholder("5")
                                .setRequired(true)
                                .build())
                        .build();

                event.replyModal(modal).queue();
            }
            case "delete" -> {
                vc.delete().queue();
                voiceSet.remove(voice);
                event.reply("🧹 Канал удалён").setEphemeral(true).queue();
            }
            default -> {
                event.reply("Неизвестное действие").setEphemeral(true).queue();
            }
        }

        voice.setup(settings);
        sendPanel(voice);
    }

    @EventHandler
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("voice:rename")) {
            Member member = event.getMember();
            if (member == null) return;

            PrivateVoice voice = getVoiceChannel(member);
            if (voice == null) {
                event.reply("У тебя нет приватного канала").setEphemeral(true).queue();
                return;
            }

            String newName = event.getValue("name").getAsString();
            if (newName.length() < 1 || newName.length() > 100) {
                event.reply("❌ Имя должно быть от 1 до 100 символов").setEphemeral(true).queue();
                return;
            }

            voice.getSettings().put("name", newName);
            voice.getChannel().getManager().setName(newName).queue();

            event.reply("✅ Канал переименован в **" + newName + "**").setEphemeral(true).queue();
            return;
        }

        if (event.getModalId().equals("voice:set_limit")) {

            Member member = event.getMember();
            if (member == null) return;

            PrivateVoice voice = getVoiceChannel(member);
            if (voice == null) {
                event.reply("У тебя нет приватного канала").setEphemeral(true).queue();
                return;
            }

            String input = event.getValue("limit").getAsString();
            int limit;
            try {
                limit = Integer.parseInt(input);
                if (limit < 0 || limit > 99) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                event.reply("❌ Укажи число от 0 до 99").setEphemeral(true).queue();
                return;
            }

            voice.getSettings().put("max", limit);
            voice.getChannel().getManager().setUserLimit(limit).queue();

            event.reply("✅ Лимит установлен на **" + limit + "** человек").setEphemeral(true).queue();
        }
    }

    @EventHandler
    public void onEntitySelect(EntitySelectInteractionEvent event) {
        String componentId = event.getComponentId();
        Member member = event.getMember();
        if (member == null || member.getUser().isBot()) return;

        if (!componentId.equals("voice:blacklist_select") && !componentId.equals("voice:whitelist_select")) return;

        PrivateVoice voice = getVoiceChannel(member);
        if (voice == null) {
            event.reply("У тебя нет приватного канала").setEphemeral(true).queue();
            return;
        }

        String listKey = componentId.contains("blacklist") ? "blacklist" : "whitelist";
        TypeMap settings = voice.getSettings();

        List<String> userIds = event.getEntitlements().stream()
                .map(entity -> entity.getId())
                .toList();

        settings.put(listKey, userIds);

        event.reply("✅ Участники обновлены в " + (listKey.equals("blacklist") ? "чёрном" : "белом") + " списке").setEphemeral(true).queue();

        voice.setup(settings);
    }
}
