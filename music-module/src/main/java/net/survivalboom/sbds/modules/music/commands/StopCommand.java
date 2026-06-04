package net.survivalboom.sbds.modules.music.commands;

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

@CommandClass(name = "stop", description = "Stops current playing music bot", translationKey = "music.command.stop")
public class StopCommand extends CommandBase implements SlashCommandExecutor, ConsoleCommandExecutor {

    private final MusicManager manager;

    public StopCommand(@NotNull MusicManager manager) {
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

        if (Utils.checkInteractionDenied(manager, info, player, false)) {
            return;
        }

        player.shutdown();

        info.reply("music.command.stop.success")
                .withPlaceholders("bot", player.getBot().getBot().getSelfUser().getAsMention())
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();

        GuildPlayer player = Utils.getConsolePlayer(manager, info, channel, false);
        if (player == null) {
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
