package net.survivalboom.sbds.moderation.module.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.moderation.api.moderation.IAuditManager;
import net.survivalboom.sbds.moderation.api.moderation.PunishmentType;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.moderation.module.ModerationModule;
import net.survivalboom.sbds.moderation.module.storage.audit.AuditEntry;
import net.survivalboom.sbds.moderation.module.storage.audit.AuditRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AuditManager extends Manager implements IAuditManager {

    private final ModerationModule module;

    private final AuditRepositoryHandler repository;


    public AuditManager(@NotNull ModerationModule module) {
        this.module = module;
        this.repository = new AuditRepositoryHandler();
    }


    @Override
    protected void init0() {
        module.createRepository("audit", repository);
    }

    @Override
    protected void shutdown0() {}

    @Override
    public @NotNull CompletableFuture<List<IAuditEntry>> getRecords(

            @Nullable User user,
            @Nullable Guild guild,

            @Nullable PunishmentType type,
            @Nullable PunishmentType.Action action

    ) {
        return repository.getRecords(user, guild, type, action).thenApply(r -> r.stream().map(v -> (IAuditEntry) v).toList());
    }

    @Override
    public @NotNull CompletableFuture<@Nullable IAuditEntry> getRecord(long id) {
        return repository.getById(id).thenApply(r -> r);
    }


    public @NotNull CompletableFuture<AuditEntry> addRecord(@NotNull AuditEntry record) {
        return repository.addRecord(record);
    }

}
