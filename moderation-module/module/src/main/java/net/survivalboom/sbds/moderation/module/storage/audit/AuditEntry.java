package net.survivalboom.sbds.moderation.module.storage.audit;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.api.moderation.PunishmentType;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.moderation.api.storage.IPunishmentData;
import net.survivalboom.sbds.moderation.module.storage.Punishment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "sbds_moderation_audit")
public class AuditEntry extends Punishment implements IAuditEntry {

    @Column
    private long punishmentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PunishmentType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PunishmentType.Action action;

    public AuditEntry(

            long punishmentId,

            @NotNull Guild guild,
            @NotNull User user,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable User moderator,

            @NotNull Instant time,
            @Nullable Instant end,

            @NotNull PunishmentType type,
            @NotNull PunishmentType.Action action

    ) {
        super(guild, user, reason, comment, moderator, time, end, true, null);

        Objects.requireNonNull(type, "type == null");

        this.punishmentId = punishmentId;
        this.type = type;
        this.action = action;

    }

    protected AuditEntry() {
        super(null);
    }

    @Override
    public @NotNull PunishmentType getType() {
        return type;
    }

    @Override
    public @NotNull PunishmentType.Action getAction() {
        return action;
    }

    @Override
    public long getPunishmentId() {
        return punishmentId;
    }

    @Override
    public @Nullable IPunishmentData getPunishment() {
        return null;
    }



    public static @NotNull AuditEntry createFromPunishment(@NotNull IPunishmentData punishment, @NotNull PunishmentType.Action action) {

        long id = punishment.getId();
        PunishmentType type = punishment.getType();

        Guild guild = punishment.getGuild();
        User user = punishment.getUser();

        User moderator = punishment.getModerator();

        Instant time = punishment.getTime();
        Instant end = punishment.getEnd();

        String reason = punishment.getReason();
        String comment = punishment.getComment();

        return new AuditEntry(id, guild, user, reason, comment, moderator, time, end, type, action);

    }


}
