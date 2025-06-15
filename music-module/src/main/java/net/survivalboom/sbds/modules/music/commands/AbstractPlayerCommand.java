package net.survivalboom.sbds.modules.music.commands;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.interaction.IInteractionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.bots.MusicBot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class AbstractPlayerCommand extends CommandBase implements SlashCommand, ConsoleCommand {

    protected final BotManager botManager;

    public AbstractPlayerCommand(@NotNull BotManager botManager) {
        this.botManager = botManager;
    }

    protected @Nullable GuildPlayer getPlayer(@NotNull IInteractionInfo info, boolean create, boolean ephemeral) {

        Member member = info.member();
        if (member == null) return null;

        AudioChannelUnion channel = Objects.requireNonNull(member.getVoiceState()).getChannel();
        if (channel == null) {
            info.reply("music.not-in-voice").send().setEphemeral(ephemeral).queue();
            return null;
        }

        GuildPlayer player = botManager.findCurrentPlayer(channel);
        if (player == null && !create) {
            info.reply("music.no-bot-in-voice").send().setEphemeral(ephemeral).queue();
            return null;
        }

        if (player == null) {

            List<MusicBot> freeBots = botManager.findFreeBots(channel);
            if (freeBots.isEmpty()) {
                info.reply("music.command.play.no-free-bot").send().setEphemeral(ephemeral).queue();
                return null;
            }

            MusicBot bot = freeBots.getFirst();
            player = bot.createPlayer(channel.getGuild());

        }

        return player;

    }

    protected @Nullable GuildPlayer getPlayer(@NotNull ConsoleExecutionInfo info, @NotNull AudioChannelUnion channel, boolean create) {

        GuildPlayer player = botManager.findCurrentPlayer(channel);
        if (player == null && !create) {
            info.logger().info("There is no music bot in your voice channel.");
            return null;
        }

        if (player == null) {

            List<MusicBot> freeBots = botManager.findFreeBots(channel);
            if (freeBots.isEmpty()) {
                info.logger().info("No free bot found!");
                return null;
            }

            MusicBot bot = freeBots.getFirst();
            player = bot.createPlayer(channel.getGuild());

        }

        return player;

    }


    protected @NotNull String createTracksString(@NotNull List<Track> tracks, boolean withIndex, int max) {

        StringBuilder builder = new StringBuilder();
        int i = 1;
        for (Track ignored : tracks) {

            if (i > tracks.size()) break;

            if (i >= max && i < tracks.size()) {
                builder.append("- `..").append(tracks.size() - max).append("..`\n");
                i = tracks.size();
                continue;
            }

            TrackInfo info = tracks.get(i - 1).getInfo();
            Placeholders placeholders = new Placeholders();
            placeholders
                    .add("{INDEX}", i)
                    .add("{NAME}", info.getTitle())
                    .add("{DURATION}", formatTime(info.getLength()))
                    .add("{SOURCE}", info.getSourceName())
                    .add("{LINK}", info.getUri());

            if (withIndex) builder.append(placeholders.parse("`{INDEX}.` **[{NAME}]({LINK})** `{DURATION}`\n"));
            else builder.append(placeholders.parse("- **[{NAME}]({LINK})** `{DURATION}`\n"));

            i++;

        }

        return builder.toString();

    }

    protected boolean checkBannedOrLocked(@NotNull IInteractionInfo info, @NotNull GuildPlayer player, boolean ephemeral) {

        Guild guild = info.guild();
        Objects.requireNonNull(guild);

        User user = info.user();
        Objects.requireNonNull(user);

        if (botManager.isMusicBanned(guild, user)) {
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

    protected static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
