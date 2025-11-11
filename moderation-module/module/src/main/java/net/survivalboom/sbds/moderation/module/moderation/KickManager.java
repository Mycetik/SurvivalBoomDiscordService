package net.survivalboom.sbds.moderation.module.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.api.moderation.IKickManager;
import net.survivalboom.sbds.moderation.api.moderation.PunishmentType;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.moderation.module.ModerationModule;
import net.survivalboom.sbds.moderation.module.storage.audit.AuditEntry;
import net.survivalboom.sbds.moderation.module.utils.ModerationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class KickManager extends ModerationManager implements IKickManager {

    public KickManager(@NotNull ModerationModule module) {
        super(module);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull IAuditEntry> kick(

            @NotNull Member member,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    ) {

        Guild guild = member.getGuild();
        User user = member.getUser();

        return guild.kick(user).reason(reason).submit()
                .thenCompose(v -> auditManager.addRecord(new AuditEntry(0, guild, user, reason, comment, moderator, Instant.now(), null, PunishmentType.KICK, PunishmentType.Action.ADD)))
                .thenApply(v -> v);

    }

    @Override
    public @NotNull CompletableFuture<IAuditEntry> kick(
            @NotNull Guild guild,
            @NotNull User user,
            @Nullable User moderator,
            @Nullable String reason,
            @Nullable String comment
    ) {
        checkValid();
        return guild.kick(user).reason(reason).submit()
                .thenCompose(v -> auditManager.addRecord(new AuditEntry(0, guild, user, reason, comment, moderator, Instant.now(), null, PunishmentType.KICK, PunishmentType.Action.ADD)))
                .thenApply(v -> v);
    }

}
