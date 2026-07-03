package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
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
import net.survivalboom.sbds.api.commands.string.StringCommandExecutor;
import net.survivalboom.sbds.api.commands.string.StringExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.modules.music.MusicModule;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import net.survivalboom.sbds.modules.music.music.LoopMode;
import net.survivalboom.sbds.modules.music.utils.Utils;
import org.jetbrains.annotations.NotNull;

@CommandClass(
        name = "loop",
        description = "Sets a loop mode for current music bot",
        translationKey = "music.command.loop",
        permission = "music.command.loop",
        defaultPermission = true
)
public class LoopCommand extends CommandBase implements SlashCommandExecutor, StringCommandExecutor, ConsoleCommandExecutor {

    private final MusicManager manager;

    public LoopCommand(@NotNull MusicModule module) {
        this.manager = module.getMusicManager();
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        executes0(info);
    }

    @Override
    public void executes(@NotNull StringExecutionInfo info) {
        executes0(info);
    }

    private void executes0(@NotNull InteractionHolder info) {

        LoopMode mode = info.arguments().getCast("mode", LoopMode.class).orElseThrow();

        GuildPlayer player = Utils.getInteractionPlayer(manager, info, false, false);
        if (player == null) {
            return;
        }

        if (Utils.checkInteractionDenied(manager, info, player, false)) {
            return;
        }

        player.setLoopMode(mode);

        User botUser = player.getBot().getBot().getSelfUser();

        info.reply("music.command.loop.success")
                .withPlaceholders(
                        "bot", botUser,
                        "mode", mode
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

        boolean newState = !player.hasAdminLock(); // toggle lock
        player.setAdminLock(newState);

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
