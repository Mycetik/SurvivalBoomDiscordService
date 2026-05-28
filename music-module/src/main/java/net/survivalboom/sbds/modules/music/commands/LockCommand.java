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
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import net.survivalboom.sbds.modules.music.music.GuildPlayer;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "music-lock", description = "Locks current music bot for staff usage only", translationKey = "music.command.lock", permission = "music.command.lock")
public class LockCommand extends AbstractPlayerCommand {

    public LockCommand(@NotNull MusicManager musicManager) {
        super(musicManager);
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) {
            return;
        }

        boolean state = !player.adminLock();
        player.adminLock(state);

        User botUser = player.getBot().getBot().getSelfUser();

        String str = state ? "music.command.lock.locked" : "music.command.lock.unlocked";
        info.reply(str)
                .withPlaceholders("bot", botUser)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().getCast("channel", AudioChannelUnion.class).orElseThrow();

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().error("No player found for the provided channel.");
            return;
        }

        boolean newState = !player.adminLock(); // toggle lock
        player.adminLock(newState);

        String msg = newState ? "Music bot is now &blocked &rfor staff-only use." : "Music bot is now &bunlocked &rfor all users.";
        info.logger().info(msg);

    }

    @ArgumentMethod(description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
