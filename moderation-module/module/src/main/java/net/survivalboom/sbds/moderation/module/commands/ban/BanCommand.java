package net.survivalboom.sbds.moderation.module.commands.ban;

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
import net.survivalboom.sbds.moderation.module.commands.AbstractModerationCommand;
import net.survivalboom.sbds.moderation.module.moderation.ModerationManager;
import net.survivalboom.sbds.moderation.module.storage.records.Ban;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

@Command(name = "ban", description = "Bans user in a guild", translationKey = "moderation.command.ban", permission = "moderation.command.ban")
public class BanCommand extends AbstractModerationCommand {

    private final ModerationManager moderationManager;


    public BanCommand(@NotNull ModerationManager moderationManager) {
        this.moderationManager = moderationManager;
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
            info.reply("moderation.invalid-duration").queue();
            return;
        }

        info.reply("sbds.loading").queue();

        Ban ban = moderationManager.getBan(guild, user).join();
        if (ban != null) {
            info.editHook("moderation.command.ban.already-banned").withPlaceholders("{MEMBER}", user.getAsMention()).queue();
            return;
        }

        User responsible = info.user();

        ban = moderationManager.ban(guild, user, duration, responsible, reason, comment).join();

        info.editHook("moderation.ban.success")
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

        Ban ban = moderationManager.getBan(guild, user).join();
        if (ban != null) {
            info.logger().error("User `{}` already has a ban `{}` in the guild `{}`.", user, ban, guild);
            return;
        }

        moderationManager.ban(guild, user, duration, null, reason, comment).join();

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
