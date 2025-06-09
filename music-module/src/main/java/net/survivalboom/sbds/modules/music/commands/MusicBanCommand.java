package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "music-ban", description = "Ban a member from using music bot", permission = "music.command.musicban")
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


    @CommandArgument(name = "target", description = "A member to ban/unban")
    public Argument<?> target() {
        return new UserArgument();
    }

}
