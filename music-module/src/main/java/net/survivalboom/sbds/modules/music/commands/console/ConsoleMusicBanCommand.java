package net.survivalboom.sbds.modules.music.commands.console;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import org.jetbrains.annotations.NotNull;

@Command(name = "ban", description = "Ban or unban a user from using the music bot (console)")
public class ConsoleMusicBanCommand extends CommandBase implements ConsoleCommand {

    private final BotManager botManager;

    public ConsoleMusicBanCommand(@NotNull BotManager botManager) {
        this.botManager = botManager;
    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {
        Integer guild_id = info.arguments().get("guild", Integer.class);
        User user = info.arguments().get("user", User.class);

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

    @CommandArgument(name = "guild", description = "The guild")
    public Argument<?> guildArg() {
        return new IntegerArgument();
    }

    @CommandArgument(name = "user", description = "The user to ban or unban")
    public Argument<?> userArg() {
        return new UserArgument();
    }
}
