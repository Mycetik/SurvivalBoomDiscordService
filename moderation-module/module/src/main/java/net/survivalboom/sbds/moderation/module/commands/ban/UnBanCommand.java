package net.survivalboom.sbds.moderation.module.commands.ban;

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
import net.survivalboom.sbds.moderation.module.commands.AbstractModerationCommand;
import net.survivalboom.sbds.moderation.module.moderation.ModerationManager;
import net.survivalboom.sbds.moderation.module.storage.records.Ban;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "unban", description = "Removes a ban from a user in a guild", permission = "moderation.command.unban", translationKey = "moderation.command.unban")
public class UnBanCommand extends AbstractModerationCommand {

    private final ModerationManager moderationManager;


    public UnBanCommand(@NotNull ModerationManager manager) {
        this.moderationManager = manager;
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        User user = info.arguments().getCastNotNull("user", User.class);

        Guild guild = info.guild();
        Objects.requireNonNull(guild, "guild == null");

        info.reply("sbds.loading").queue();

        Ban ban = moderationManager.getBan(guild, user).join();
        if (ban == null) {
            info.editHook("moderation.commands.unban.not-banned").withPlaceholders("{MEMBER}", user.getAsMention()).queue();
            return;
        }

        User responsible = info.user();

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        moderationManager.removeBan(ban, responsible, reason, comment).join();

        info.editHook("moderation.commands.unban.success").withPlaceholders("{MEMBER}", user.getAsMention()).queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        User user = info.arguments().getCastNotNull("user", User.class);
        Guild guild = info.arguments().getCastNotNull("guild", Guild.class);

        Ban ban = moderationManager.getBan(guild, user).join();
        if (ban == null) {
            info.logger().error("User `{}` is not banned on the guild `{}`.", user, guild);
            return;
        }

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        moderationManager.removeBan(ban, null, reason, comment).join();

        info.logger().info("Successfully unbanned user `{}` on the guild `{}`.", user, guild);

    }


    @CommandArgument(name = "guild", scope = ArgumentScope.CONSOLE)
    public GuildArgument guild() {
        return new GuildArgument();
    }

    @CommandArgument(name = "user", index = 1)
    public UserArgument user() {
        return new UserArgument();
    }

    @CommandArgument(name = "reason", index = 2, required = false)
    public StringArgument reason() {
        return new StringArgument();
    }

    @CommandArgument(name = "comment", index = 3, required = false)
    public StringArgument comment() {
        return new StringArgument();
    }

}
