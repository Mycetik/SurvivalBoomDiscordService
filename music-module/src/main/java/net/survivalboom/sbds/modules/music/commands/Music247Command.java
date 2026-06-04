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
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.utils.Utils;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "music-24-7", description = "Disable disconnect on idle for the current music bot", translationKey = "music.command.24-7", permission = "music.command.24_7")
public class Music247Command extends CommandBase implements SlashCommandExecutor, ConsoleCommandExecutor {

    private final MusicManager manager;

    public Music247Command(@NotNull MusicManager manager) {
        this.manager = manager;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) throws Throwable {

        GuildPlayer player = Utils.getInteractionPlayer(manager, info, false, false);
        if (player == null) {
            return;
        }

        if (Utils.checkInteractionDenied(manager, info, player, false)) {
            return;
        }

        boolean state = !player.idleDisconnect();
        player.idleDisconnect(state);

        String str = state ? "music.command.24-7.disable" : "music.command.24-7.enable";

        User botUser = player.getBot().getBot().getSelfUser();
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

        boolean newState = !player.idleDisconnect();
        player.idleDisconnect(newState);

        info.logger().info("24/7 mode is now {}", newState ? "ENABLED (idle disconnect disabled)" : "DISABLED (idle disconnect enabled)");

    }

    @ArgumentMethod(description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
