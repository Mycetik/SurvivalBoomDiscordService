package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.MusicBot;
import net.survivalboom.sbds.modules.music.music.MusicTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public abstract class AbstractPlayerCommand extends CommandBase implements SlashCommandExecutor, ConsoleCommandExecutor {

    protected final MusicManager musicManager;

    public AbstractPlayerCommand(@NotNull MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    protected @Nullable GuildPlayer getPlayer(@NotNull SlashExecutionInfo info, boolean create, boolean ephemeral) {

        Member member = info.member();
        if (member == null) {
            return null;
        }

        AudioChannelUnion channel = Objects.requireNonNull(member.getVoiceState()).getChannel();
        if (channel == null) {
            info.reply("music.not-in-voice").send().setEphemeral(ephemeral).queue();
            return null;
        }

        GuildPlayer player = musicManager.findCurrentPlayer(channel);
        if (player == null && !create) {
            info.reply("music.no-bot-in-voice").send().setEphemeral(ephemeral).queue();
            return null;
        }

        if (player == null) {

            List<MusicBot> freeBots = musicManager.findFreeBots(channel);
            if (freeBots.isEmpty()) {
                info.reply("music.command.play.no-free-bot").send().setEphemeral(ephemeral).queue();
                return null;
            }

            MusicBot bot = freeBots.getFirst();
            player = bot.createPlayer(channel);

        }

        return player;

    }

    protected @Nullable GuildPlayer getPlayer(@NotNull ConsoleExecutionInfo info, @NotNull AudioChannelUnion channel, boolean create) {

        GuildPlayer player = musicManager.findCurrentPlayer(channel);
        if (player == null && !create) {
            info.logger().info("There is no music bot in the channel.");
            return null;
        }

        if (player == null) {

            List<MusicBot> freeBots = musicManager.findFreeBots(channel);
            if (freeBots.isEmpty()) {
                info.logger().info("No free bot found!");
                return null;
            }

            MusicBot bot = freeBots.getFirst();
            player = bot.createPlayer(channel);

        }

        return player;

    }


    protected @NotNull String createTracksString(@NotNull List<MusicTrack> tracks, int max) {

        List<String> formatted = IntStream.of(0, Math.min(tracks.size(), max) - 1)
                .mapToObj(index -> {

                    MusicTrack track = tracks.get(index);

                    Placeholders placeholders = track.placeholders();
                    placeholders.add("track.index", index);

                    String string = "`{track.index}.` **[{track.title}]({track.link})** `{track.duration}`";

                    return placeholders.parse(string);

                })
                .toList();

        String string = String.join("\n", formatted);
        if (tracks.size() > max) {
            string += "\n...";
        }

        return string;

    }

    protected boolean checkBannedOrLocked(@NotNull SlashExecutionInfo info, @NotNull GuildPlayer player, boolean ephemeral) {

        Guild guild = info.guild();
        Objects.requireNonNull(guild);

        User user = info.user();
        Objects.requireNonNull(user);

        if (musicManager.isMusicBanned(guild, user)) {
            info.reply("music.command.music-ban.denied").send().setEphemeral(ephemeral).queue();
            return true;
        }

        if (player.adminLock() && !info.hasPermission("music.command.lock.bypass")) {
            User botUser = player.getBot().getBot().getSelfUser();
            info.reply("music.command.lock.denied")
                    .withPlaceholders("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator(), "{BOT-AVATAR}", botUser.getEffectiveAvatarUrl())
                    .send()
                    .setEphemeral(ephemeral)
                    .queue();
            return true;
        }

        return false;

    }

}
