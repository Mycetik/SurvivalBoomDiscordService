package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractPlayerCommand extends CommandBase implements SlashCommand {

    private final BotManager botManager;

    public AbstractPlayerCommand(@NotNull BotManager botManager) {
        this.botManager = botManager;
    }

    protected @Nullable GuildPlayer getPlayer(@NotNull SlashExecutionInfo info) {

        Member member = info.guildMember();
        if (member == null) return null;

        AudioChannelUnion channel = Objects.requireNonNull(member.getVoiceState()).getChannel();
        if (channel == null) {
            info.reply("command.music-module.not-in-voice").queue();
            return null;
        }

        GuildPlayer player = botManager.findCurrentPlayer(channel);
        if (player == null) {
            info.reply("commands.music-module.no-bot-in-voice").queue();
            return null;
        }

        return player;

    }

}
