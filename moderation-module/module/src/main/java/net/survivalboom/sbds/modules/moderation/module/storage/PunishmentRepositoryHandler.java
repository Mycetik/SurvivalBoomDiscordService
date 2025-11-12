package net.survivalboom.sbds.modules.moderation.module.storage;

import jakarta.persistence.criteria.Predicate;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.modules.moderation.module.utils.ExpiringModerationManager;
import net.survivalboom.sbds.modules.moderation.module.utils.ExpiringQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PunishmentRepositoryHandler<T extends Punishment> extends RepositoryHandler<T> {

    private final ExpiringQueue expiringQueue;


    private final boolean allowDuplications;

    private final PunishmentCreator<T> punishmentCreator;


    public PunishmentRepositoryHandler(

            @NotNull Class<T> clazz,
            boolean allowDuplicates,
            @NotNull PunishmentCreator<T> punishmentCreator,

            @NotNull ExpiringQueue queue

    ) {

        super(clazz);

        this.allowDuplications = allowDuplicates;
        this.punishmentCreator = punishmentCreator;

        this.expiringQueue = queue;

    }

    //
    // EXPIRING
    //

    public void loadExpiring(ExpiringModerationManager<T> manager) {

        var result = sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(dataRecordClass);
            var root = query.from(dataRecordClass);

            query.select(root).where(cb.isNotNull(root.get("end")));

            return session.createQuery(query).getResultList();

        }, false).join();

        result.forEach(v -> expiringQueue.queue(manager, v));

    }

    public @NotNull CompletableFuture<@NotNull List<T>> getPunishments(
            @Nullable Guild guild,
            @Nullable User user
    ) {

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(dataRecordClass);
            var root = query.from(dataRecordClass);

            List<Predicate> predicates = new ArrayList<>();

            if (user != null) {
                predicates.add(cb.equal(root.get("user"), user));
            }

            if (guild != null) {
                predicates.add(cb.equal(root.get("guild"), guild));
            }

            var qs = cb.and(predicates.toArray(new Predicate[0]));
            query.select(root).where(qs);

            return session.createQuery(query).getResultList();

        }, true);

    }


    public @NotNull CompletableFuture<T> createPunishment(

            @NotNull Guild guild,
            @NotNull User user,

            @Nullable Duration duration,

            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment

    ) {

        if (duration != null && duration.getSeconds() < 10) {
            throw new IllegalArgumentException("Duration < 10 seconds. " + duration.getSeconds());
        }

        Instant currentTime = Instant.now();
        Instant endTime = duration != null ? currentTime.plus(duration) : null;

        return getPunishments(guild, user).thenCompose(list -> {

            if (!allowDuplications && !list.isEmpty()) {
                throw new IllegalStateException("User `" + user.getName() + "` already has a punishment `" + dataRecordClass.getSimpleName() + "` in guild `" + guild.getName() + "`");
            }

            T punishment = punishmentCreator.create(guild, user, currentTime, endTime, responsible, reason, comment);

            return save(punishment);

        });

    }

    public @NotNull CompletableFuture<Void> removePunishment(@NotNull T punishment) {
        return delete(punishment);
    }


    @FunctionalInterface
    public interface PunishmentCreator<T extends Punishment> {

        @NotNull T create(

                @NotNull Guild guild,
                @NotNull User user,

                @NotNull Instant time,
                @Nullable Instant endTime,

                @Nullable User responsible,
                @Nullable String reason,
                @Nullable String comment

        );

    }



}
