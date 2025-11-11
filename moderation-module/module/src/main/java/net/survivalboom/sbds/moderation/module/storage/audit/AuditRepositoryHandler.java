package net.survivalboom.sbds.moderation.module.storage.audit;

import jakarta.persistence.criteria.Predicate;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.moderation.api.moderation.PunishmentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AuditRepositoryHandler extends RepositoryHandler<AuditEntry> {

    public AuditRepositoryHandler() {
        super(AuditEntry.class);
    }


    public @NotNull CompletableFuture<List<AuditEntry>> getRecords(

            @Nullable User user,
            @Nullable Guild guild,

            @Nullable PunishmentType type,
            @Nullable PunishmentType.Action action

    ) {

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(AuditEntry.class);
            var root = query.from(AuditEntry.class);

            List<Predicate> predicates = new ArrayList<>();

            if (user != null) {
                predicates.add(cb.equal(root.get("user"), user));
            }

            if (guild != null) {
                predicates.add(cb.equal(root.get("guild"), guild));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }

            var qs = cb.and(predicates.toArray(new Predicate[0]));
            query.select(root).where(qs);

            return session.createQuery(query).getResultList();

        }, true);

    }

    public @NotNull CompletableFuture<AuditEntry> addRecord(@NotNull AuditEntry record) {
        return save(record);
    }


}
