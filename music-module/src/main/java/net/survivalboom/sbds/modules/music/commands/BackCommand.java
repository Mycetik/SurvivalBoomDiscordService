package net.survivalboom.sbds.modules.music.commands;

import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.IInteractionInfo;
import net.survivalboom.sbds.api.interaction.component.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Command(name = "back", description = "Returns the previous song", translationKey = "music.command.back")
public class BackCommand extends AbstractPlayerCommand {

    public BackCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
        sbds.getButtonInteractionManager().registerListener(module, "back", this::onButtonClick);
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

        int steps = info instanceof SlashExecutionInfo slashExecutionInfo ? slashExecutionInfo.arguments().getCastOrDefault("steps", Integer.class, 1) : 1;
        int allowedSteps = player.getPlayingIndex();

        if (steps < 1) {
            info.reply("music.command.skip.invalid-index").withPlaceholders("{PLAYLIST-SIZE}", allowedSteps).send().setEphemeral(ephemeral).queue();
            return;
        }

        TrackInfo skippedTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();

        try {
            player.changePlayingIndex(-steps);
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

        info.reply(steps == 1 ? "music.command.back.single" : "music.command.back.multiple")
                .withPlaceholders(placeholders)
                .send()
                .setEphemeral(ephemeral)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
        int steps = info.arguments().getCastOrDefault("steps", Integer.class, 1);

        if (channel == null || steps < 1) {
            info.logger().warn("Invalid channel or back count.");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().warn("No player found for the given channel.");
            return;
        }

        int allowedSteps = player.getPlayingIndex();
        if (steps > allowedSteps) {
            info.logger().warn("Cannot go back {} tracks. Current index: {}", steps, allowedSteps);
            return;
        }

        TrackInfo skippedTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();

        try {
            player.changePlayingIndex(-steps);
        } catch (IllegalArgumentException e) {
            info.logger().warn("Invalid back index. Current index: {}", player.getPlayingIndex());
            return;
        }

        TrackInfo playingTrack = Objects.requireNonNull(player.getCurrentPlaying()).getInfo();

        info.logger().info("Went back {} track(s): {} -> {}", steps, skippedTrack.getTitle(), playingTrack.getTitle());

    }

    @CommandArgument(name = "steps", description = "Songs to skip", required = false, index = 1)
    public Argument<?> songs() {
        return new IntegerArgument();
    }

    @CommandArgument(name = "channel", description = "Channel with music bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
