package net.survivalboom.sbds.modules.music.commands;

import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.IInteractionInfo;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@CommandClass(name = "skip", description = "Skips the current playing song", translationKey = "music.command.skip")
public class SkipCommand extends AbstractPlayerCommand {

    public SkipCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
        sbds.getButtonInteractionManager().registerListener(module, "next", this::onButtonClick);
    }

    public void onButtonClick(@NotNull ButtonInteractionInfo info) {
        executes0(info, true);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        executes0(info, false);
    }


    private void executes0(@NotNull IInteractionInfo info, boolean ephemeral) {

        GuildPlayer player = getPlayer(info, false, ephemeral);
        if (player == null) return;

        if (checkBannedOrLocked(info, player, ephemeral)) return;

        if (player.isLastTrack()) {
            player.stop();
            info.reply("music.command.stop.success").send().setEphemeral(ephemeral).queue();
            return;
        }

        int steps = info instanceof SlashExecutionInfo slashExecutionInfo ? slashExecutionInfo.arguments().getCastOrDefault("steps", Integer.class, 1) : 1;
        int allowedSteps = player.getPlaylistSize() - player.getPlayingIndex();

        if (steps < 1) {
            info.reply("music.command.skip.invalid-index").withPlaceholders("{PLAYLIST-SIZE}", allowedSteps).send().setEphemeral(ephemeral).queue();
            return;
        }

        TrackInfo skippedTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();

        try {
            player.changePlayingIndex(steps);
        }

        catch (IllegalArgumentException e) {
            info.reply("music.command.skip.invalid-index").withPlaceholders("{PLAYLIST-SIZE}", allowedSteps).send().setEphemeral(ephemeral).queue();
            return;
        }

        TrackInfo playingTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();
        User botUser = player.getBot().getBot().getSelfUser();

        Placeholders placeholders = new Placeholders()
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl())

                .add("{SKIPPED-NAME}", skippedTrack.getTitle())
                .add("{SKIPPED-DURATION}", formatTime(skippedTrack.getLength()))
                .add("{SKIPPED-SOURCE}", skippedTrack.getSourceName())
                .add("{SKIPPED-LINK}", skippedTrack.getUri())
                .add("{SKIPPED-COUNT}", steps)

                .add("{PLAYING-NAME}", playingTrack.getTitle())
                .add("{PLAYING-DURATION}", formatTime(playingTrack.getLength()))
                .add("{PLAYING-SOURCE}", playingTrack.getSourceName())
                .add("{PLAYING-LINK}", playingTrack.getUri())

                .add("{COUNT}", steps)

                .add("{PLAYLIST-SIZE}", player.getPlaylist().size())
                .add("{PLAYLIST}", createTracksString(player.getPlaylist(), true, 10));

        info.reply(steps == 1 ? "music.command.skip.single" : "music.command.skip.multiple")
                .withPlaceholders(placeholders)
                .send()
                .setEphemeral(ephemeral)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCastOrNull("channel", AudioChannelUnion.class);
        Objects.requireNonNull(channel);

        int steps = info.arguments().getCastOrDefault("steps", Integer.class, 1);

        if (steps < 1) {
            info.logger().warn("Invalid skip count.");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().warn("No player found for the given channel.");
            return;
        }

        if (player.isLastTrack()) {
            player.stop();
            info.logger().info("Last track reached. Stopping player.");
            return;
        }

        TrackInfo skippedTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();

        try {
            player.changePlayingIndex(steps);
        } catch (IllegalArgumentException e) {
            info.logger().warn("Invalid skip index. Playlist size: {}", player.getPlaylistSize());
            return;
        }

        TrackInfo playingTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();

        info.logger().info("Skipped {} song(s): {} -> {}", steps, skippedTrack.getTitle(), playingTrack.getTitle());

    }

    @ArgumentMethod(name = "channel", description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

    @ArgumentMethod(name = "steps", description = "Songs to skip", required = false)
    public Argument<?> songs() {
        return new IntegerArgument();
    }

}
