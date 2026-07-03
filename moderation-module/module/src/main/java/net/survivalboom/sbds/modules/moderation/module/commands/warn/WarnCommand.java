package net.survivalboom.sbds.modules.moderation.module.commands.warn;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.discord.GuildArgument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.GreedyStringArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.modules.moderation.module.commands.AbstractModerationCommand;
import net.survivalboom.sbds.modules.moderation.module.moderation.WarnManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

@CommandClass(name = "warn", description = "Warns user in a guild", translationKey = "moderation.command.warn", permission = "moderation.command.warn")
public class WarnCommand extends AbstractModerationCommand {

    private final WarnManager warnManager;


    public WarnCommand(@NotNull WarnManager warnManager) {
        this.warnManager = warnManager;
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) throws Throwable {

        Guild guild = info.guild();
        Objects.requireNonNull(guild, "guild == null");

        User user = info.arguments().getCastNotNull("user", User.class);

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        String durationRaw = info.arguments().getCastOrNull("time", String.class);
        Duration duration = durationRaw != null ? CommonUtils.getDurationFromStr(durationRaw) : null;

        if (durationRaw != null && duration == null) {
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

        User moderator = info.user();

        info.reply("sbds.loading").queue();

        var warn = warnManager.warn(guild, user, moderator, reason, comment, duration).join();

        info.editHook("moderation.command.warn.success").withPlaceholders(createPunishmentPlaceholders(warn)).queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        Guild guild = info.arguments().getCastNotNull("guild", Guild.class);
        User user = info.arguments().getCastNotNull("user", User.class);

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        String durationRaw = info.arguments().getCastOrNull("time", String.class);
        Duration duration = durationRaw != null ? CommonUtils.getDurationFromStr(durationRaw) : null;

        if (durationRaw != null && duration == null) {
            info.logger().error("Invalid duration `{}`. Example: 1d 2h 20s", durationRaw);
            return;
        }

        warnManager.warn(guild, user, null, reason, comment, duration).join();

        info.logger().info("Successfully warned `{}` in the `{}`.", user, guild);

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

    @ArgumentMethod(name = "time", index = 4, required = false)
    public GreedyStringArgument time() {
        return new GreedyStringArgument();
    }

}
