package net.survivalboom.sbds.modules.moderation.module.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.modules.moderation.api.moderation.IBanManager;
import net.survivalboom.sbds.modules.moderation.api.moderation.PunishmentType;
import net.survivalboom.sbds.modules.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.modules.moderation.api.storage.IBanData;
import net.survivalboom.sbds.modules.moderation.module.ModerationModule;
import net.survivalboom.sbds.modules.moderation.module.storage.PunishmentRepositoryHandler;
import net.survivalboom.sbds.modules.moderation.module.storage.audit.AuditEntry;
import net.survivalboom.sbds.modules.moderation.module.storage.records.Ban;
import net.survivalboom.sbds.modules.moderation.module.utils.ExpiringModerationManager;
import net.survivalboom.sbds.modules.moderation.module.utils.ExpiringQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BanManager extends ExpiringModerationManager<Ban> implements IBanManager {

    public BanManager(
            @NotNull ExpiringQueue expiringQueue,
            @NotNull ModerationModule module
    ) {
        super(new PunishmentRepositoryHandler<>(Ban.class, false, Ban::create, expiringQueue), expiringQueue, module);
    }

    @Override
    protected void init0() {
        module.createRepository("bans", repository);
    }

    @Override
    public @NotNull CompletableFuture<IBanData> ban(

            @NotNull Guild guild,
            @NotNull User user,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    ) {

        return punish(guild, user, moderator, reason, comment, duration)
                .thenCompose(ban -> guild.ban(user, 0, TimeUnit.SECONDS).reason(reason).submit().thenApply(v -> ban));

    }

    @Override
    public @NotNull CompletableFuture<@NotNull IBanData> ban(

            @NotNull Member member,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    ) {

        Guild guild = member.getGuild();
        User user = member.getUser();

        return punish(guild, user, moderator, reason, comment, duration)
                .thenCompose(ban -> guild.ban(user, 0, TimeUnit.SECONDS).reason(reason).submit().thenApply(v -> ban));

    }

    @Override
    public @NotNull CompletableFuture<@NotNull IAuditEntry> removeBan(

            @NotNull IBanData banData,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    ) {

        var ban = (Ban) banData;

        return unPunish(ban, moderator, reason, comment).thenCompose(v -> {

            ban.getGuild().unban(ban.getUser()).reason(reason).complete();

            return auditManager.addRecord(AuditEntry.createFromPunishment(ban, PunishmentType.Action.REMOVE)).thenApply(suka -> suka);

        });

    }

    @Override
    public @NotNull CompletableFuture<List<IBanData>> getCurrent(@Nullable Guild guild, @Nullable User user) {
        return getCurrent0(guild, user).thenApply(v -> v.stream().map(b -> (IBanData) b).toList());
    }

}
