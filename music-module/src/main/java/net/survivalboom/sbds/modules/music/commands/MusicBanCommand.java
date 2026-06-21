package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
import net.survivalboom.sbds.api.commands.string.StringCommandExecutor;
import net.survivalboom.sbds.api.commands.string.StringExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.modules.music.MusicModule;
import net.survivalboom.sbds.modules.music.music.MusicManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@CommandClass(
        name = "music-ban",
        description = "Bans member from using music bot",
        translationKey = "music.command.music-ban",
        permission = "music.command.music-ban"
)
public class MusicBanCommand extends CommandBase implements SlashCommandExecutor, StringCommandExecutor, ConsoleCommandExecutor {

    private final MusicManager manager;

    public MusicBanCommand(@NotNull MusicModule module) {
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

        User target = info.arguments().getCast("target", User.class).orElseThrow();
        Member member = info.guild().retrieveMember(target).complete();

        boolean state = !manager.isMusicBanned(member);
        manager.setMusicBanned(member, state);

        String str = state ? "music.command.music-ban.banned" : "music.command.music-ban.unbanned";
        info.reply(str)
                .withPlaceholders("user", target)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        Guild guild = info.arguments().getCast("guild", Guild.class).orElseThrow();
        User user = info.arguments().getCast("target", User.class).orElseThrow();
        Member member = guild.retrieveMember(user).complete();

        info.logger().info("Retrieving data from the database...");

        boolean banned = !manager.isMusicBanned(member);
        manager.setMusicBanned(member, banned);

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
