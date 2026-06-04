package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.utils.Utils;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "pause", description = "Pauses or resumes current playing track", translationKey = "music.command.pause")
public class PauseCommand extends CommandBase implements SlashCommandExecutor, ConsoleCommandExecutor {

    protected final MusicManager manager;

    public PauseCommand(@NotNull MusicManager manager) {
        this.manager = manager;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        executes0(info, false);
    }

    private void executes0(@NotNull InteractionHolder info, boolean ephemeral) {

        GuildPlayer player = Utils.getInteractionPlayer(manager, info, false, ephemeral);
        if (player == null) {
            return;
        }

        if (Utils.checkInteractionDenied(manager, info, player, ephemeral)) {
            return;
        }

        boolean state = !player.paused();
        player.paused(state);

        User botUser = player.getBot().getBot().getSelfUser();
        String str = state ? "music.command.pause.paused" : "music.command.pause.resumed";
        info.reply(str)
                .withPlaceholders("bot", botUser)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();

        GuildPlayer player = Utils.getConsolePlayer(manager, info, channel, false);
        if (player == null) {
            return;
        }

        boolean newState = !player.paused();
        player.paused(newState);

        User botUser = player.getBot().getBot().getSelfUser();
        String stateStr = newState ? "Paused" : "Resumed";
        info.logger().info("{} playback for bot {} in channel: {}", stateStr, botUser.getAsTag(), channel.getName());

    }

    @ArgumentMethod(description = "The voice channel", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
