package net.survivalboom.sbds.modules.moderation.module.commands.ban;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.GuildArgument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.modules.moderation.module.commands.AbstractModerationCommand;
import net.survivalboom.sbds.modules.moderation.module.moderation.BanManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

@Command(name = "ban", description = "Bans user in a guild", translationKey = "moderation.command.ban", permission = "moderation.command.ban")
public class BanCommand extends AbstractModerationCommand {

    private final BanManager banManager;


    public BanCommand(@NotNull BanManager banManager) {
        this.banManager = banManager;
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        User user = info.arguments().getCastNotNull("user", User.class);
        Guild guild = info.guild();

        Objects.requireNonNull(guild, "guild == null");

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        String durationRaw = info.arguments().getCastOrNull("time", String.class);
        Duration duration = durationRaw != null ? CommonUtils.getDurationFromStr(durationRaw) : null;
        if (duration == null && durationRaw != null) {
            info.reply("moderation.invalid-duration").withPlaceholders("{string}", durationRaw).queue();
            return;
        }

        if (user.isBot()) {
            info.reply("moderation.punishment-bot").queue();
            return;
        }

        if (info.sbds().getPermissionManager().hasPermission(guild.getIdLong(), user.getIdLong(), "moderation.ban.immune", false)) {
            info.reply("moderation.punishment-denied").queue();
            return;
        }

        if (info.user().equals(user)) {
            info.reply("moderation.punishment-self").queue();
            return;
        }

        info.reply("sbds.loading").queue();

        var result = banManager.getCurrent(guild, user).join();
        if (!result.isEmpty()) {
            info.editHook("moderation.command.ban.already-banned").withPlaceholders("{user}", user.getAsMention()).queue();
            return;
        }

        User moderator = info.user();

        var ban = banManager.ban(guild, user, moderator, reason, comment, duration).join();

        info.editHook("moderation.command.ban.success")
                .withPlaceholders(createPunishmentPlaceholders(ban))
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        User user = info.arguments().getCastNotNull("user", User.class);
        Guild guild = info.arguments().getCastNotNull("guild", Guild.class);

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        String durationRaw = info.arguments().getCastOrNull("time", String.class);
        Duration duration = durationRaw != null ? CommonUtils.getDurationFromStr(durationRaw) : null;
        if (duration == null && durationRaw != null) {
            info.logger().error("Invalid duration string `{}`. Example: `2h 30h 10s`", durationRaw);
            return;
        }

        var result = banManager.getCurrent(guild, user).join();
        if (!result.isEmpty()) {
            info.logger().error("User `{}` already has a ban `{}` in the guild `{}`.", user, result.getFirst(), guild);
            return;
        }

        banManager.ban(guild, user, null, reason, comment, duration).join();

        info.logger().info("User `{}` successfully banned in the guild `{}`.", user, guild);

    }

    // ARGUMENTS //

    @CommandArgument(name = "user", description = "A user to ban")
    public Argument<?> user() {
        return new UserArgument();
    }

    @CommandArgument(name = "guild", index = 1, description = "A guild where to ban", scope = ArgumentScope.CONSOLE)
    public Argument<?> guild() {
        return new GuildArgument();
    }

    @CommandArgument(name = "reason", index = 2, description = "A reason to ban", required = false)
    public Argument<?> reason() {
        return new StringArgument();
    }

    @CommandArgument(name = "time", index = 3, description = "A duration of the ban", required = false)
    public Argument<?> time() {
        return new StringArgument();
    }

    @CommandArgument(name = "comment", index = 4, description = "Comment from a moderator for the ban", required = false)
    public Argument<?> comment() {
        return new StringArgument();
    }

}
