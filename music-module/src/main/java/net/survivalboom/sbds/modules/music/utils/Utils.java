package net.survivalboom.sbds.modules.music.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.MusicBot;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.MusicTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class Utils {

    // COMMAND //

    public static @Nullable GuildPlayer getInteractionPlayer(
            @NotNull MusicManager manager,
            @NotNull InteractionHolder info,
            boolean create,
            boolean ephemeral
    ) {

        Member member = info.member();
        if (member == null) {
            return null;
        }

        AudioChannelUnion channel = Objects.requireNonNull(member.getVoiceState()).getChannel();
        if (channel == null) {
            info.reply("music.not-in-voice").setEphemeral(ephemeral).queue();
            return null;
        }

        GuildPlayer player = manager.findCurrentPlayer(channel);
        if (player == null && !create) {
            info.reply("music.no-bot-in-voice").setEphemeral(ephemeral).queue();
            return null;
        }

        if (player == null) {

            List<MusicBot> freeBots = manager.findFreeBots(channel);
            if (freeBots.isEmpty()) {
                info.reply("music.command.play.no-free-bot").setEphemeral(ephemeral).queue();
                return null;
            }

            MusicBot bot = freeBots.getFirst();
            player = bot.createPlayer(channel.getGuild());

        }

        return player;

    }

    public static @Nullable GuildPlayer getConsolePlayer(
            @NotNull MusicManager manager,
            @NotNull ConsoleExecutionInfo info,
            @NotNull AudioChannelUnion channel,
            boolean create
    ) {

        GuildPlayer player = manager.findCurrentPlayer(channel);
        if (player == null && !create) {
            info.logger().info("There is no music bot in the channel.");
            return null;
        }

        if (player == null) {

            List<MusicBot> freeBots = manager.findFreeBots(channel);
            if (freeBots.isEmpty()) {
                info.logger().info("No free bot found!");
                return null;
            }

            MusicBot bot = freeBots.getFirst();
            player = bot.createPlayer(channel.getGuild());

        }

        return player;

    }

    public static @NotNull String createTracksString(@NotNull List<MusicTrack> tracks, int max) {

        int limit = Math.max(0, Math.min(tracks.size(), max));
        if (limit == 0) {
            return !tracks.isEmpty() ? "..." : "";
        }

        List<String> formatted = IntStream.range(0, limit)
                .mapToObj(index -> {
                    MusicTrack track = tracks.get(index);

                    Placeholders placeholders = Placeholders.of(
                            "track.index", index + 1,
                            "track", track
                    );

                    String template = "`{track.index}.` **[{track.title}]({track.link})** `{track.duration}`";
                    return placeholders.parse(template);
                })
                .toList();

        String result = String.join("\n", formatted);

        if (tracks.size() > max) {
            result += "\n...";
        }

        return result;

    }

    public static boolean checkInteractionDenied(
            @NotNull MusicManager manager,
            @NotNull InteractionHolder info,
            @NotNull GuildPlayer player,
            boolean ephemeral
    ) {

        if (manager.isMusicBanned(info.member())) {
            info.reply("music.command.music-ban.denied").setEphemeral(ephemeral).queue();
            return true;
        }

        if (!player.isActive()) {
            return false;
        }

        if (player.hasAdminLock() && !info.hasPermission("music.command.lock.bypass")) {
            User botUser = player.getBot().getBot().getSelfUser();
            info.reply("music.command.lock.denied")
                    .withPlaceholders("bot", botUser)
                    .setEphemeral(ephemeral)
                    .queue();
            return true;
        }

        return false;

    }

}
