package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "pause", description = "Pauses or resumes current playing track", translationKey = "music.command.pause")
public class PauseCommand extends AbstractPlayerCommand {

    public PauseCommand(@NotNull MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) {
            return;
        }

        if (checkBannedOrLocked(info, player, false)) {
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

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().warn("No active player found for the given channel.");
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
