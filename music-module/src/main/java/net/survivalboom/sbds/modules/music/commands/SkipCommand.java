package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.commands.string.StringCommandExecutor;
import net.survivalboom.sbds.api.commands.string.StringExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.modules.music.MusicModule;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.MusicTrack;
import net.survivalboom.sbds.modules.music.utils.Utils;
import org.jetbrains.annotations.NotNull;

@CommandClass(
        name = "skip",
        description = "Skips the current playing song",
        translationKey = "music.command.skip",
        permission = "music.command.skip",
        defaultPermission = true
)
public class SkipCommand extends CommandBase implements SlashCommandExecutor, StringCommandExecutor, ConsoleCommandExecutor {

    private final MusicModule module;

    private final MusicManager manager;

    public SkipCommand(@NotNull MusicModule module) {
        this.module = module;
        this.manager = module.getMusicManager();
    }

    @Override
    protected void init(@NotNull ISBDS sbds) {

        sbds.getComponentInteractionManager().registerListener(
                module,
                "next",
                ButtonInteractionEvent.class,
                click -> executes0(click, 1, true)
        );

    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        int steps = info.arguments().getCast("steps", Integer.class).orElse(1);
        executes0(info, steps, false);
    }

    @Override
    public void executes(@NotNull StringExecutionInfo info) {
        int steps = info.arguments().getCast("steps", Integer.class).orElse(1);
        executes0(info, steps, false);
    }

    private void executes0(@NotNull InteractionHolder info, int steps, boolean ephemeral) {

        GuildPlayer player = Utils.getInteractionPlayer(manager, info, false, ephemeral);
        if (player == null) return;

        if (Utils.checkInteractionDenied(manager, info, player, ephemeral)) {
            return;
        }

        if (player.isLastTrack()) {
            player.disconnect();
            info.reply("music.command.stop.success").setEphemeral(ephemeral).queue();
            return;
        }

        MusicTrack skippedTrack = player.getCurrentPlaying();

        try {
            player.changePlayingIndex(steps);
        }

        catch (IllegalArgumentException e) {
            info.reply("music.command.skip.invalid-index")
                    .withPlaceholders("steps", player.getPlaylistSize() - player.getPlayingIndex())
                    .setEphemeral(ephemeral)
                    .queue();
            return;
        }

        MusicTrack playingTrack = player.getCurrentPlaying();
        User botUser = player.getBot().getBot().getSelfUser();

        info.reply(steps == 1 ? "music.command.skip.single" : "music.command.skip.multiple")
                .withPlaceholders(
                        "bot", botUser,
                        "skipped", skippedTrack,
                        "playing", playingTrack,
                        "count", steps,
                        "playlist", Utils.createTracksString(player.getPlaylist(), 10),
                        "playlist.size", player.getPlaylistSize()
                )
                .setEphemeral(ephemeral)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();
        int steps = info.arguments().getCast("steps", Integer.class).orElse(1);

        GuildPlayer player = Utils.getConsolePlayer(manager, info, channel, false);
        if (player == null) {
            return;
        }

        if (player.isLastTrack()) {
            player.disconnect();
            info.logger().info("Last track reached. Stopping player.");
            return;
        }

        MusicTrack skippedTrack = player.getCurrentPlaying();

        try {
            player.changePlayingIndex(steps);
        } catch (IllegalArgumentException e) {
            info.logger().warn("Invalid skip index. Playlist size: {}", player.getPlaylistSize());
            return;
        }

        MusicTrack playingTrack = player.getCurrentPlaying();

        info.logger().info("Skipped {} song(s): {} -> {}", steps, skippedTrack.getTitle(), playingTrack.getTitle());

    }

    @ArgumentMethod(description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

    @ArgumentMethod(description = "Songs to skip", required = false)
    public Argument<?> steps() {
        return new IntegerArgument();
    }

}
