package net.survivalboom.sbds.moderation.module.storage;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.moderation.module.moderation.ExpiringQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
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

    public void loadExpiring() {

        var result = sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(dataRecordClass);
            var root = query.from(dataRecordClass);

            query.select(root).where(cb.isNotNull(root.get("end")));

            return session.createQuery(query).getResultList();

        }, false).join();

        result.forEach(v -> expiringQueue.queue(this, v));

    }


    public @NotNull CompletableFuture<@Nullable T> getPunishment(@NotNull Guild guild, @NotNull User user) {

        if (allowDuplications) {
            throw new IllegalStateException("There may be multiple punishments in this repository for one member of a guild. Please use getPunishments(guild, user) instead");
        }

        Objects.requireNonNull(guild, "guild == null");
        Objects.requireNonNull(user, "user == null");

        long id = DataRecord.hash(guild.getIdLong(), user.getIdLong());

        return getById(id);

    }

    public @NotNull CompletableFuture<@NotNull List<T>> getPunishments(@NotNull Guild guild, @NotNull User user) {

        if (!allowDuplications) {
            throw new IllegalStateException("There will be only one punishment per member in a guild. Please use getPunishment(guild, user) instead");
        }

        Objects.requireNonNull(guild, "guild == null");
        Objects.requireNonNull(user, "user == null");

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(dataRecordClass);
            var root = query.from(dataRecordClass);

            query.select(root).where(cb.equal(root.get("guild"), guild), cb.equal(root.get("user"), user));

            return session.createQuery(query).getResultList();

        }, true);

    }


    public @NotNull CompletableFuture<List<T>> getGuildPunishments(@NotNull Guild guild) {

        Objects.requireNonNull(guild, "guild == null");

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(dataRecordClass);
            var root = query.from(dataRecordClass);

            query.select(root).where(cb.equal(root.get("guild"), guild));

            return session.createQuery(query).getResultList();

        }, true);

    }

    public @NotNull CompletableFuture<List<T>> getUserPunishments(@NotNull User user) {

        Objects.requireNonNull(user, "user == null");

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(dataRecordClass);
            var root = query.from(dataRecordClass);

            query.select(root).where(cb.equal(root.get("user"), user));

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

        if (duration != null && duration.getSeconds() > 10) {
            throw new IllegalArgumentException("Duration < 10 seconds");
        }

        Instant currentTime = Instant.now();
        Instant endTime = duration != null ? currentTime.plus(duration) : null;

        return getPunishment(guild, user).thenCompose(p -> {

            if (!allowDuplications) {

                if (p != null) {
                    throw new IllegalStateException("User `" + user.getName() + "` already has a punishment `" + dataRecordClass.getSimpleName() + "` in guild `" + guild.getName() + "`");
                }

            }

            T punishment = punishmentCreator.create(guild, user, currentTime, endTime, responsible, reason, comment);

            var future = save(punishment);
            if (endTime != null) {
                return future.thenApply(v -> {
                    expiringQueue.queue(this, punishment);
                    return v;
                });
            }

            return future;

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
