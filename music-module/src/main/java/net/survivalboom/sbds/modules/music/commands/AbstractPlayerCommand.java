package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.bots.MusicBot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class AbstractPlayerCommand extends CommandBase implements SlashCommand {

    private final BotManager botManager;

    public AbstractPlayerCommand(@NotNull BotManager botManager) {
        this.botManager = botManager;
    }

    protected @Nullable GuildPlayer getPlayer(@NotNull SlashExecutionInfo info, boolean create) {

        Member member = info.guildMember();
        if (member == null) return null;

        AudioChannelUnion channel = Objects.requireNonNull(member.getVoiceState()).getChannel();
        if (channel == null) {
            info.reply("command.music-module.not-in-voice").queue();
            return null;
        }

        GuildPlayer player = botManager.findCurrentPlayer(channel);
        if (player == null && !create) {
            info.reply("commands.music-module.no-bot-in-voice").queue();
            return null;
        }

        if (player == null) {

            List<MusicBot> freeBots = botManager.findFreeBots(channel);
            if (freeBots.isEmpty()) {
                info.reply("music.command.play.no-free-bot").queue();
                return null;
            }

            MusicBot bot = freeBots.getFirst();
            player = bot.createPlayer(channel.getGuild());

        }

        return player;

    }

}
