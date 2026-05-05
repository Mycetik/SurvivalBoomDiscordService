package net.survivalboom.sbds.modules.moderation.module.commands.kick;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.discord.GuildArgument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.modules.moderation.module.commands.AbstractModerationCommand;
import net.survivalboom.sbds.modules.moderation.module.moderation.KickManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@CommandClass(name = "kick", description = "Kicks user from the guild", translationKey = "moderation.command.kick", permission = "moderation.command.kick")
public class KickCommand extends AbstractModerationCommand {


    private final KickManager kickManager;


    public KickCommand(@NotNull KickManager kickManager) {
        this.kickManager = kickManager;
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        Guild guild = info.guild();
        Objects.requireNonNull(guild, "guild == null");

        User user = info.arguments().getCastNotNull("user", User.class);

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        User moderator = info.user();

        if (user.isBot()) {
            info.reply("moderation.punishment-bot").queue();
            return;
        }

        if (info.sbds().getPermissionManager().hasPermission(guild.getIdLong(), user.getIdLong(), "moderation.kick.immune", false)) {
            info.reply("moderation.punishment-denied").queue();
            return;
        }

        if (info.user().equals(user)) {
            info.reply("moderation.punishment-self").queue();
            return;
        }

        info.reply("sbds.loading").queue();

        var result = kickManager.kick(guild, user, moderator, reason, comment).join();

        info.editHook("moderation.command.kick.success").withPlaceholders(createPunishmentPlaceholders(result)).queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        Guild guild = info.arguments().getCastNotNull("guild", Guild.class);
        User user = info.arguments().getCastNotNull("user", User.class);

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        kickManager.kick(guild, user, null, reason, comment).join();

        info.logger().info("Successfully kicked `{}` from the `{}`", user, guild);

    }


    @ArgumentMethod(name = "guild", scope = ArgumentScope.CONSOLE)
    public GuildArgument guild() {
        return new GuildArgument();
    }

    @ArgumentMethod(name = "user", index = 1)
    public UserArgument user() {
        return new UserArgument();
    }

    @ArgumentMethod(name = "reason", index = 2, required = false)
    public StringArgument reason() {
        return new StringArgument();
    }

    @ArgumentMethod(name = "comment", index = 3, required = false)
    public StringArgument comment() {
        return new StringArgument();
    }

}
