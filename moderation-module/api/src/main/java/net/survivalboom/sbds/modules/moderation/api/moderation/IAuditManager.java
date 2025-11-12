package net.survivalboom.sbds.modules.moderation.api.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.modules.moderation.api.storage.IAuditEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IAuditManager {

    @NotNull CompletableFuture<List<IAuditEntry>> getRecords(

            @Nullable User user,
            @Nullable Guild guild,

            @Nullable PunishmentType type,
            @Nullable PunishmentType.Action action

    );

    @NotNull CompletableFuture<@Nullable IAuditEntry> getRecord(long id);

}
