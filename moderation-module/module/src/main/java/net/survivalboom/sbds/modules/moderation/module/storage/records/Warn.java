package net.survivalboom.sbds.modules.moderation.module.storage.records;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.modules.moderation.api.moderation.PunishmentType;
import net.survivalboom.sbds.modules.moderation.api.storage.IWarnData;
import net.survivalboom.sbds.modules.moderation.module.storage.Punishment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Entity
@Table(name = "sbds_moderation_warns")
public class Warn extends Punishment implements IWarnData {

    public Warn(

            @NotNull Guild guild,
            @NotNull User user,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable User responsible,

            @NotNull Instant time,
            @Nullable Instant end

    ) {
        super(guild, user, reason, comment, responsible, time, end, true, PunishmentType.WARN);
    }

    protected Warn() {
        super(PunishmentType.WARN);
    }

    public static @NotNull Warn create(
            @NotNull Guild guild,
            @NotNull User user,
            @NotNull Instant time,
            @Nullable Instant endTime,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {
        return new Warn(guild, user, reason, comment, responsible, time, endTime);
    }

}
