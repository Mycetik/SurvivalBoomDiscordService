package net.survivalboom.sbds.moderation.module.commands.mute;

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
import net.survivalboom.sbds.moderation.module.storage.records.Mute;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "unmute", description = "Removes a mute from a user in a guild", permission = "moderation.command.unmute", translationKey = "moderation.command.unmute")
public class UnMuteCommand extends AbstractModerationCommand {

    private final ModerationManager moderationManager;


    public UnMuteCommand(@NotNull ModerationManager manager) {
        this.moderationManager = manager;
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        User user = info.arguments().getCastNotNull("user", User.class);

        Guild guild = info.guild();
        Objects.requireNonNull(guild, "guild == null");

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        User responsible = info.user();

        info.reply("sbds.loading").queue();

        Mute mute = moderationManager.getMute(guild, user).join();
        if (mute == null) {
            info.editHook("moderation.command.unmute.not-muted").withPlaceholders("{MEMBER}", user.getAsMention()).queue();
            return;
        }

        moderationManager.removeMute(mute, responsible, reason, comment).join();

        info.editHook("moderation.command.unmute.success").withPlaceholders("{MEMBER}", user.getAsMention()).queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        User user = info.arguments().getCastNotNull("user", User.class);
        Guild guild = info.arguments().getCastNotNull("guild", Guild.class);

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        Mute mute = moderationManager.getMute(guild, user).join();
        if (mute == null) {
            info.logger().error("User `{}` is not muted on the guild `{}`.", user, guild);
            return;
        }

        moderationManager.removeMute(mute, null, reason, comment).join();

        info.logger().info("Successfully unmuted user `{}` on the guild `{}`.", user, guild);

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
