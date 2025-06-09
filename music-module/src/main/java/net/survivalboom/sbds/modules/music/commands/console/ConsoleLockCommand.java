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

@Command(name = "music-lock", description = "Lock or unlock music bot for staff-only usage")
public class ConsoleLockCommand extends AbstractPlayerCommand implements ConsoleCommand {

    public ConsoleLockCommand(@NotNull BotManager botManager) {
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
            info.logger().error("Channel argument is missing or invalid.");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().error("No player found for the provided channel.");
            return;
        }

        boolean newState = !player.adminLock(); // toggle lock
        player.adminLock(newState);

        if (newState) {
            info.logger().info("Music bot is now **locked** for staff-only use.");
        } else {
            info.logger().info("Music bot is now **unlocked** for all users.");
        }
    }

    @CommandArgument(name = "channel", description = "Voice channel with the bot")
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }
}
