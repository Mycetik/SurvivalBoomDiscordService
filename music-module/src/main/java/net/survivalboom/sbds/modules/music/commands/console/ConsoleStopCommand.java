package net.survivalboom.sbds.modules.music.commands.console;

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

@Command(name = "music-stop", description = "Stopping a music ")
public class ConsoleStopCommand extends AbstractPlayerCommand implements ConsoleCommand {
    public ConsoleStopCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
        assert channel != null;

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) return;

        player.stop();
        info.logger().info("Bot stopped");
    }


    @CommandArgument(name = "channel")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }
}
