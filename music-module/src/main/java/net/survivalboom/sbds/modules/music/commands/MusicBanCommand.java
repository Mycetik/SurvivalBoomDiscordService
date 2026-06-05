package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.GuildArgument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@CommandClass(name = "music-ban", description = "Bans member from using music bot", translationKey = "music.command.music-ban", permission = "music.command.musicban")
public class MusicBanCommand extends CommandBase implements SlashCommandExecutor, ConsoleCommandExecutor {

    private final MusicManager manager;

    public MusicBanCommand(@NotNull MusicManager manager) {
        this.manager = manager;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        Guild guild = info.guild();
        Objects.requireNonNull(guild);

        User target = info.arguments().getCast("target", User.class).orElseThrow();

        boolean state = !manager.isMusicBanned(guild, target);
        manager.setMusicBanned(guild, target, state);

        String str = state ? "music.command.music-ban.banned" : "music.command.music-ban.unbanned";
        info.reply(str)
                .withPlaceholders("user", target)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        Guild guild = info.arguments().getCast("guild", Guild.class).orElseThrow();
        User user = info.arguments().getCast("target", User.class).orElseThrow();

        info.logger().info("Retrieving data from the database...");

        boolean banned = !manager.isMusicBanned(guild, user);
        manager.setMusicBanned(guild, user, banned);

        String result = banned ? "User has been music-banned" : "User has been unbanned from music";
        info.logger().info("{}: {} ({})", result, user.getAsTag(), user.getId());

    }

    @ArgumentMethod(description = "Guild", scope = ArgumentScope.CONSOLE)
    public Argument<?> guild() {
        return new GuildArgument();
    }


    @ArgumentMethod(description = "Member", index = 1)
    public Argument<?> target() {
        return new UserArgument();
    }

}
