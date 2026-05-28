package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "stop", description = "Stops current playing music bot", translationKey = "music.command.stop")
public class StopCommand extends AbstractPlayerCommand {

    public StopCommand(@NotNull MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) return;

        if (checkBannedOrLocked(info, player, false)) return;

        player.shutdown();
        info.reply("music.command.stop.success")
                .withPlaceholders("bot", player.getBot().getBot().getSelfUser().getAsMention())
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().error("No player was found in that channel.");
            return;
        }

        player.shutdown();
        info.logger().info("Stopping `{}`.", player.getBot().getBot().getSelfUser().getEffectiveName());

    }

    @ArgumentMethod(description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
