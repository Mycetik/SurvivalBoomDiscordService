package net.survivalboom.sbds.modules.moderation.module.commands.mute;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.discord.GuildArgument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.modules.moderation.module.commands.AbstractModerationCommand;
import net.survivalboom.sbds.modules.moderation.module.moderation.MuteManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

@Command(name = "mute", description = "Mutes user in the guild", translationKey = "moderation.command.mute", permission = "moderation.command.mute")
public class MuteCommand extends AbstractModerationCommand {

    private final MuteManager muteManager;


    public MuteCommand(@NotNull MuteManager muteManager) {
        this.muteManager = muteManager;
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        Guild guild = info.guild();
        Objects.requireNonNull(guild, "guild == null; command can be used only in a guild!");

        User user = info.arguments().getCastNotNull("user", User.class);
        User responsible = info.user();

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

        var result = muteManager.getCurrent(guild, user).join();
        if (!result.isEmpty()) {
            info.editHook("moderation.command.mute.already-muted").withPlaceholders("{user}", user.getAsMention()).queue();
            return;
        }

        var mute = muteManager.mute(guild, user, responsible, reason, comment, duration).join();

        info.editHook("moderation.command.mute.success")
                .withPlaceholders(createPunishmentPlaceholders(mute))
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        Guild guild = info.arguments().getCastNotNull("guild", Guild.class);
        User user = info.arguments().getCastNotNull("user", User.class);

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        String durationRaw = info.arguments().getCastOrNull("time", String.class);
        Duration duration = durationRaw != null ? CommonUtils.getDurationFromStr(durationRaw) : null;
        if (duration == null) {
            info.logger().error("Invalid duration string `{}`. Example: `2h 30m 10s`", durationRaw);
            return;
        }

        var result = muteManager.getCurrent(guild, user).join();
        if (!result.isEmpty()) {
            info.logger().error("User `{}` already has a mute `{}`.", user, result.getFirst());
            return;
        }

        muteManager.mute(guild, user, null, reason, comment, duration).join();

        info.logger().info("Successfully muted `{}` in `{}`.", user, guild);

    }

    @CommandArgument(name = "user", description = "A user to mute")
    public UserArgument user() {
        return new UserArgument();
    }

    @CommandArgument(name = "guild", index = 1, description = "A guild where to mute", scope = ArgumentScope.CONSOLE)
    public GuildArgument guild() {
        return new GuildArgument();
    }

    @CommandArgument(name = "reason", index = 2, description = "A reason of the mute", required = false)
    public StringArgument reason() {
        return new StringArgument();
    }

    @CommandArgument(name = "comment", index = 3, description = "A comment to a reason for the mute", required = false)
    public StringArgument comment() {
        return new StringArgument();
    }

    @CommandArgument(name = "time", index = 4, description = "A duration of the mute", required = false)
    public StringArgument time() {
        return new StringArgument();
    }

}
