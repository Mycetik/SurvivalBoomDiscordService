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

import java.util.List;

@CommandClass(
        name = "back",
        description = "Returns the previous song",
        translationKey = "music.command.back",
        permission = "music.command.back",
        defaultPermission = true
)
public class BackCommand extends CommandBase implements SlashCommandExecutor, StringCommandExecutor, ConsoleCommandExecutor {

    private final MusicModule module;

    private final MusicManager manager;

    public BackCommand(@NotNull MusicModule module) {
        this.module = module;
        this.manager = module.getMusicManager();
    }

    @Override
    protected void init(@NotNull ISBDS sbds) {

        sbds.getComponentInteractionManager().registerListener(
                module,
                "back",
                ButtonInteractionEvent.class,
                click -> executes0(click, 1, true)
        );

    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) throws Throwable {
        int steps = info.arguments().getCast("steps", Integer.class).orElse(1);
        executes0(info, steps, false);
    }

    @Override
    public void executes(@NotNull StringExecutionInfo info) throws Throwable {
        int steps = info.arguments().getCast("steps", Integer.class).orElse(1);
        executes0(info, steps, false);
    }

    private void executes0(@NotNull InteractionHolder info, int steps, boolean ephemeral) {

        GuildPlayer player = Utils.getInteractionPlayer(manager, info, false, ephemeral);
        if (player == null) {
            return;
        }

        if (Utils.checkInteractionDenied(manager, info, player, ephemeral)) {
            return;
        }

        MusicTrack skippedTrack = player.getCurrentPlaying();

        try {
            player.changePlayingIndex(-steps);
        }

        catch (IllegalArgumentException e) {
            info.reply("music.command.skip.invalid-index")
                    .withPlaceholders("steps", player.getPlayingIndex())
                    .setEphemeral(ephemeral)
                    .queue();
            return;
        }

        MusicTrack playingTrack = player.getCurrentPlaying();
        User botUser = player.getBot().getBot().getSelfUser();

        List<MusicTrack> playlist = player.getPlaylist();

        info.reply(steps == 1 ? "music.command.back.single" : "music.command.back.multiple")
                .withPlaceholders(
                        "bot", botUser,
                        "skipped", skippedTrack,
                        "playing", playingTrack,
                        "count", steps,
                        "playlist.size", playlist.size(),
                        "playlist", Utils.createTracksString(playlist, 10)
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

        int allowedSteps = player.getPlayingIndex();
        if (steps > allowedSteps) {
            info.logger().warn("Cannot go back {} tracks. Current index: {}", steps, allowedSteps);
            return;
        }

        MusicTrack skippedTrack = player.getCurrentPlaying();

        try {
            player.changePlayingIndex(-steps);
        } catch (IllegalArgumentException e) {
            info.logger().warn("Invalid back index. Current index: {}", player.getPlayingIndex());
            return;
        }

        MusicTrack playingTrack = player.getCurrentPlaying();

        info.logger().info("Went back {} track(s): {} -> {}", steps, skippedTrack.getTitle(), playingTrack.getTitle());

    }

    @ArgumentMethod(description = "Songs to skip", required = false, index = 1)
    public Argument<?> steps() {
        return new IntegerArgument();
    }

    @ArgumentMethod(description = "Channel with music bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
