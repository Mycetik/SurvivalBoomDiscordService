package net.survivalboom.sbds.moderation.api.storage;

import net.survivalboom.sbds.moderation.api.moderation.PunishmentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IAuditEntry extends IPunishmentData {

    @NotNull PunishmentType getType();

    @NotNull PunishmentType.Action getAction();

    long getPunishmentId();

    @Nullable IPunishmentData getPunishment();

}
