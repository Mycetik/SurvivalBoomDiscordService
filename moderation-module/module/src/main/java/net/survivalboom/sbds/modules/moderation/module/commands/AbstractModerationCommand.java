package net.survivalboom.sbds.modules.moderation.module.commands;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.moderation.api.storage.IPunishmentData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public abstract class AbstractModerationCommand extends CommandBase implements SlashCommandExecutor, ConsoleCommand {

    protected static final String NONE = "$[values.none]";

    protected @NotNull Placeholders createPunishmentPlaceholders(@NotNull IPunishmentData punishment) {

        String user = punishment.getUser().getAsMention();

        User moderator = punishment.getModerator();
        String moderatorStr = moderator != null ? moderator.getAsMention() : NONE;

        String reason = punishment.getReason();
        if (reason == null) reason = NONE;

        String comment = punishment.getComment();
        if (comment == null) comment = NONE;

        Duration duration = punishment.getDuration();
        String durationStr = duration != null ? CommonUtils.durationToString(duration) : NONE;

        return Placeholders.of(
                "{user}", user,
                "{moderator}", moderatorStr,
                "{reason}", reason,
                "{comment}", comment,
                "{duration}", durationStr
        );

    }


}
