package net.survivalboom.sbds.modules.music.commands.console;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.commands.AbstractPlayerCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Command(name = "music-pause", description = "Pause or resume the currently playing track")
public class ConsolePauseCommand extends AbstractPlayerCommand implements ConsoleCommand {

    public ConsolePauseCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
        if (channel == null) {
            info.logger().warn("Voice channel not provided.");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().warn("No active player found for the given channel.");
            return;
        }

        boolean newState = !player.isPaused();
        player.setPaused(newState);

        User botUser = player.getBot().getBot().getSelfUser();
        String stateStr = newState ? "paused" : "resumed";

        info.logger().info("Track has been {} by {}#{}", stateStr, botUser.getName(), botUser.getDiscriminator());
    }

    @CommandArgument(name = "channel", description = "The voice channel")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }
}
