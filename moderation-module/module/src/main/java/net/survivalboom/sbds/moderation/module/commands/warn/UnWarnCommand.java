package net.survivalboom.sbds.moderation.module.commands.warn;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.moderation.module.commands.AbstractModerationCommand;
import net.survivalboom.sbds.moderation.module.moderation.WarnManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "unwarn", description = "Removes a warn from a user in the guild", translationKey = "moderation.command.unwarn", permission = "moderation.command.unwarn")
public class UnWarnCommand extends AbstractModerationCommand {

    private final WarnManager warnManager;


    public UnWarnCommand(@NotNull WarnManager warnManager) {
        this.warnManager = warnManager;
    }


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        int id = info.arguments().getCastNotNull("id", Integer.class);

        Guild guild = info.guild();
        Objects.requireNonNull(guild, "guild == null");

        User moderator = info.user();

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        info.reply("sbds.loading").queue();

        var warn = warnManager.getById(guild, id).join();
        if (warn == null) {
            info.editHook("moderation.command.unwarn.unknown").withPlaceholders("{id}", id).queue();
            return;
        }

        var result = warnManager.removeWarn(warn, moderator, reason, comment).join();

        info.editHook("moderation.command.unwarn.success").withPlaceholders(createPunishmentPlaceholders(result).add("{id}", id)).queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        int id = info.arguments().getCastNotNull("id", Integer.class);

        String reason = info.arguments().getCastOrNull("reason", String.class);
        String comment = info.arguments().getCastOrNull("comment", String.class);

        var warn = warnManager.getById(null, id).join();
        if (warn == null) {
            info.logger().error("Warn with id `{}` does not exist.", id);
            return;
        }

        warnManager.removeWarn(warn, null, reason, comment).join();

        info.logger().info("Successfully removed warn `{}`.", warn);

    }

    @CommandArgument(name = "id", index = 1)
    public IntegerArgument id() {
        return new IntegerArgument();
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
