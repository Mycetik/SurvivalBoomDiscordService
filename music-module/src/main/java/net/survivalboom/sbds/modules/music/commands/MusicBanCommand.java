package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "music-ban", description = "Bans member from using music bot", translationKey = "music.command.music-ban", permission = "music.command.musicban")
public class MusicBanCommand extends AbstractPlayerCommand {

    public MusicBanCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        Guild guild = info.guild();
        Objects.requireNonNull(guild);

        User target = info.arguments().get("target", User.class);
        Objects.requireNonNull(target);

        boolean state = !botManager.isMusicBanned(guild, target);
        botManager.setMusicBanned(guild, target, state);

        String str = state ? "music.command.music-ban.banned" : "music.command.music-ban.unbanned";
        info.reply(str).withPlaceholders("{USER}", target.getAsMention()).queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        Integer guild_id = info.arguments().get("guild", Integer.class);
        User user = info.arguments().get("target", User.class);

        if (guild_id == null || user == null) {
            info.logger().error("Missing required arguments: guild and/or user.");
            return;
        }
        Guild guild = info.sbds().getBot().getGuildById(guild_id);
        assert guild != null;

        boolean banned = !botManager.isMusicBanned(guild, user);
        botManager.setMusicBanned(guild, user, banned);

        String result = banned ? "User has been music-banned" : "User has been unbanned from music";
        info.logger().info("{}: {} ({})", result, user.getAsTag(), user.getId());

    }

    @CommandArgument(name = "guild", description = "Guild", scope = ArgumentScope.CONSOLE)
    public Argument<?> guild() {
        return new IntegerArgument();
    }


    @CommandArgument(name = "target", description = "Member", index = 1)
    public Argument<?> target() {
        return new UserArgument();
    }

}
