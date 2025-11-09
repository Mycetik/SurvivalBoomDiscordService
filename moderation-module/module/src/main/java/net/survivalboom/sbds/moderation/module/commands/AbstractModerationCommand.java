package net.survivalboom.sbds.moderation.module.commands;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.moderation.module.storage.Punishment;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractModerationCommand extends CommandBase implements SlashCommand, ConsoleCommand {

    protected @NotNull Placeholders createPunishmentPlaceholders(@NotNull Punishment punishment) {
        return Placeholders.of(
                "{user}", punishment.getUser().getAsMention(),
                "{moderator}", punishment.getResponsible(),
                "{reason}", punishment.getReason(),
                "{comment}", punishment.getComment(),
                "{duration}", CommonUtils.durationToString(punishment.getDuration())
        );
    }


}
