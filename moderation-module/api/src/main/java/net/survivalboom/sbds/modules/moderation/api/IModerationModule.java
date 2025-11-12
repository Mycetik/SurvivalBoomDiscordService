package net.survivalboom.sbds.modules.moderation.api;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.api.moderation.*;
import net.survivalboom.sbds.modules.moderation.api.moderation.*;
import net.survivalboom.sbds.modules.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.modules.moderation.api.storage.IPunishmentData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IModerationModule {

    @NotNull IAuditManager getAuditManager();

    @NotNull IBanManager getBanManager();

    @NotNull IKickManager getKickManager();

    @NotNull IMuteManager getMuteManager();

    @NotNull IWarnManager getWarnManager();

    @NotNull CompletableFuture<@NotNull IAuditEntry> removePunishment(

            @NotNull IPunishmentData punishment,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    );

}
