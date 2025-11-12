package net.survivalboom.sbds.modules.moderation.module.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.modules.moderation.api.moderation.PunishmentType;
import net.survivalboom.sbds.modules.moderation.module.ModerationModule;
import net.survivalboom.sbds.modules.moderation.module.storage.Punishment;
import net.survivalboom.sbds.modules.moderation.module.storage.PunishmentRepositoryHandler;
import net.survivalboom.sbds.modules.moderation.module.storage.audit.AuditEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ExpiringModerationManager<T extends Punishment> extends ModerationManager {

    protected final PunishmentRepositoryHandler<T> repository;

    protected final ExpiringQueue expiringQueue;


    public ExpiringModerationManager(
            @NotNull PunishmentRepositoryHandler<T> repository,
            @NotNull ExpiringQueue expiringQueue,
            @NotNull ModerationModule module
    ) {
        super(module);
        this.repository = repository;
        this.expiringQueue = expiringQueue;
    }


    //
    // ABSTRACT
    //

    protected @NotNull CompletableFuture<T> punish(

            @NotNull Guild guild,
            @NotNull User user,

            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    ) {

        checkValid();

        return repository.createPunishment(guild, user, duration, moderator, reason, comment).thenApply(v -> {
            auditManager.addRecord(AuditEntry.createFromPunishment(v, PunishmentType.Action.ADD));
            return v;
        })
        .thenApply(v -> {

            if (v.getEnd() != null) {
                expiringQueue.queue(this, v);
            }

            return v;

        });

    }

    protected @NotNull CompletableFuture<AuditEntry> unPunish(
            @NotNull T record,
            @Nullable User moderator,
            @Nullable String reason,
            @Nullable String comment
    ) {

        checkValid();

        return repository.removePunishment(record).thenRun(() -> expiringQueue.remove(record, this))
                .thenCompose(v -> auditManager.addRecord(new AuditEntry(0, record.getGuild(), record.getUser(), reason, comment, moderator, Instant.now(), null, record.getType(), PunishmentType.Action.REMOVE)));

    }


    //
    // GETTERS
    //

    public @NotNull CompletableFuture<List<T>> getCurrent0(@Nullable Guild guild, @Nullable User user) {
        return repository.getPunishments(guild, user);
    }

    public @NotNull CompletableFuture<@Nullable T> getById0(@Nullable Guild guild, long id) {
        return repository.getById(id).thenApply(v -> {

            if (v == null || guild != null && !v.getGuild().equals(guild)) {
                return v;
            }

            return v;

        });
    }

}
