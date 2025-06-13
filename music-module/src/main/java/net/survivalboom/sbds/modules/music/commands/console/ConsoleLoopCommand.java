package net.survivalboom.sbds.modules.music.commands.console;

import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.misc.EnumSelectArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.bots.LoopMode;
import net.survivalboom.sbds.modules.music.commands.AbstractPlayerCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Command(name = "loop", description = "Set loop mode for a music bot in a specified channel")
public class ConsoleLoopCommand extends AbstractPlayerCommand implements ConsoleCommand {

    public ConsoleLoopCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
        LoopMode loopMode = info.arguments().get("mode", LoopMode.class);

        if (channel == null || loopMode == null) {
            info.logger().error("Missing required arguments: channel or mode");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().error("No music player found for the specified channel.");
            return;
        }

        player.loop(loopMode);
        info.logger().info("Loop mode set to: " + loopMode);
    }

    @CommandArgument(name = "channel", description = "Voice channel where the bot is active")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

    @CommandArgument(name = "mode", description = "Loop mode to set")
    public Argument<?> mode() {
        return new EnumSelectArgument<>(LoopMode.class);
    }
}
