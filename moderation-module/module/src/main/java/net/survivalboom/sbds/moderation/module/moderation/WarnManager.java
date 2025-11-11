package net.survivalboom.sbds.moderation.module.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.api.moderation.IWarnManager;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.moderation.api.storage.IWarnData;
import net.survivalboom.sbds.moderation.module.ModerationModule;
import net.survivalboom.sbds.moderation.module.storage.PunishmentRepositoryHandler;
import net.survivalboom.sbds.moderation.module.storage.records.Warn;
import net.survivalboom.sbds.moderation.module.utils.ExpiringModerationManager;
import net.survivalboom.sbds.moderation.module.utils.ExpiringQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarnManager extends ExpiringModerationManager<Warn> implements IWarnManager {

    public WarnManager(@NotNull ExpiringQueue expiringQueue, @NotNull ModerationModule module) {
        super(new PunishmentRepositoryHandler<>(Warn.class, true, Warn::create,  expiringQueue), expiringQueue, module);
    }

    @Override
    protected void init0() {
        module.createRepository("warns", repository);
    }

    @Override
    public @NotNull CompletableFuture<IWarnData> warn(

            @NotNull Guild guild,
            @NotNull User user,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    ) {
        checkValid();
        return punish(guild, user, moderator, reason, comment, duration).thenApply(v -> v);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull IWarnData> warn(@NotNull Member member, @Nullable User moderator, @Nullable String reason, @Nullable String comment, @Nullable Duration duration) {

        checkValid();

        Guild guild = member.getGuild();
        User user = member.getUser();

        return punish(guild, user, moderator, reason, comment, duration).thenApply(v -> v);

    }

    @Override
    public @NotNull CompletableFuture<@NotNull IAuditEntry> removeWarn(

            @NotNull IWarnData warnData,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    ) {
        checkValid();

        var warn = (Warn) warnData;
        return unPunish(warn, moderator, reason, comment).thenApply(v -> v);

    }


    @Override
    public @NotNull CompletableFuture<@NotNull List<IWarnData>> getCurrent(@Nullable Guild guild, @Nullable User user) {
        return getCurrent0(guild, user).thenApply(list -> list.stream().map(v -> (IWarnData) v).toList());
    }

    @Override
    public @NotNull CompletableFuture<@Nullable IWarnData> getById(@Nullable Guild guild, long id) {
        return getById0(guild, id).thenApply(v -> v);
    }

}
