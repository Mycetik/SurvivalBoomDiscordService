package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.misc.select.EnumSelectArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.LoopMode;
import net.survivalboom.sbds.modules.music.utils.Utils;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "loop", description = "Sets a loop mode for current music bot", translationKey = "music.command.loop")
public class LoopCommand extends CommandBase implements SlashCommandExecutor, ConsoleCommandExecutor {

    private final MusicManager manager;

    public LoopCommand(@NotNull MusicManager manager) {
        this.manager = manager;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = Utils.getInteractionPlayer(manager, info, false, false);
        if (player == null) {
            return;
        }

        if (Utils.checkInteractionDenied(manager, info, player, false)) {
            return;
        }

        LoopMode loop = info.arguments().getCast("mode", LoopMode.class).orElseThrow();

        player.loop(loop);

        User botUser = player.getBot().getBot().getSelfUser();

        info.reply("music.command.loop.success")
                .withPlaceholders(
                        "bot", botUser,
                        "mode", loop
                )
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();

        GuildPlayer player = Utils.getConsolePlayer(manager, info, channel, false);
        if (player == null) {
            return;
        }

        boolean newState = !player.adminLock(); // toggle lock
        player.adminLock(newState);

        if (newState) {
            info.logger().info("Music bot is now &clocked &rfor staff-only use.");
        } else {
            info.logger().info("Music bot is now &aunlocked &rfor all users.");
        }

    }

    @ArgumentMethod(description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }


    @ArgumentMethod(description = "Loop mode", index = 1)
    public Argument<?> mode() {
        return new EnumSelectArgument<>(LoopMode.class);
    }


}
